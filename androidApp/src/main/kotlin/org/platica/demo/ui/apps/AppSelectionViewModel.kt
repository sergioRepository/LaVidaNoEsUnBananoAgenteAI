package org.platica.demo.ui.apps

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.platica.demo.data.local.AppCategoryMapper
import org.platica.demo.domain.model.AppCategory
import org.platica.demo.domain.model.TriggerApp
import org.platica.demo.domain.repository.TriggerAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppItemUi(
    val packageName: String,
    val appName: String,
    val isTrigger: Boolean,
    val category: AppCategory,
    val icon: Drawable? = null
)

data class AppSelectionUiState(
    val allApps: List<AppItemUi> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
) {
    val filteredApps: List<AppItemUi>
        get() = if (searchQuery.isBlank()) {
            allApps
        } else {
            allApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
        }

    val selectedCount: Int
        get() = allApps.count { it.isTrigger }
}

@HiltViewModel
class AppSelectionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val triggerAppRepository: TriggerAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSelectionUiState())
    val uiState: StateFlow<AppSelectionUiState> = _uiState.asStateFlow()

    init {
        loadInstalledLauncherApps()
    }

    fun loadInstalledLauncherApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            val packageManager = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfoList = packageManager.queryIntentActivities(mainIntent, 0)

            // Obtener apps ya guardadas en Room
            val savedAppsMap = triggerAppRepository.getApps().first().associateBy { it.packageName }

            val appItems = resolveInfoList
                .filter { it.activityInfo.packageName != context.packageName } // Excluir la propia app
                .distinctBy { it.activityInfo.packageName }
                .map { resolveInfo ->
                    val pkg = resolveInfo.activityInfo.packageName
                    val name = resolveInfo.loadLabel(packageManager).toString()
                    val icon = resolveInfo.loadIcon(packageManager)
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val category = AppCategoryMapper.mapCategory(appInfo, pkg)
                    val isTrigger = savedAppsMap[pkg]?.isTrigger ?: false

                    AppItemUi(
                        packageName = pkg,
                        appName = name,
                        isTrigger = isTrigger,
                        category = category,
                        icon = icon
                    )
                }
                .sortedBy { it.appName.lowercase() }

            // Sincronizar en Room
            val domainApps = appItems.map {
                TriggerApp(
                    packageName = it.packageName,
                    appName = it.appName,
                    isTrigger = it.isTrigger,
                    category = it.category
                )
            }
            triggerAppRepository.saveOrUpdateApps(domainApps)

            _uiState.update {
                it.copy(
                    allApps = appItems,
                    isLoading = false
                )
            }
        }
    }

    fun toggleApp(packageName: String, isTrigger: Boolean) {
        viewModelScope.launch {
            triggerAppRepository.setTrigger(packageName, isTrigger)
            _uiState.update { state ->
                val updated = state.allApps.map { item ->
                    if (item.packageName == packageName) item.copy(isTrigger = isTrigger) else item
                }
                state.copy(allApps = updated)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
