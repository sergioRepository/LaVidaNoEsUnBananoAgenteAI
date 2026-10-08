package com.lavidanoesunbanano.data.local.room

import com.lavidanoesunbanano.data.local.room.dao.TriggerAppDao
import com.lavidanoesunbanano.data.local.room.entity.TriggerAppEntity
import com.lavidanoesunbanano.domain.model.AppCategory
import com.lavidanoesunbanano.domain.model.TriggerApp
import com.lavidanoesunbanano.domain.repository.TriggerAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TriggerAppRepositoryImpl @Inject constructor(
    private val triggerAppDao: TriggerAppDao
) : TriggerAppRepository {

    override fun getApps(): Flow<List<TriggerApp>> {
        return triggerAppDao.getAllApps().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getTriggerPackageNames(): List<String> {
        return triggerAppDao.getTriggerPackageNames()
    }

    override suspend fun isTriggerApp(packageName: String): Boolean {
        return triggerAppDao.isTriggerApp(packageName)
    }

    override suspend fun saveOrUpdateApps(apps: List<TriggerApp>) {
        triggerAppDao.insertOrUpdateApps(apps.map { it.toEntity() })
    }

    override suspend fun setTrigger(packageName: String, isTrigger: Boolean) {
        triggerAppDao.setTrigger(packageName, isTrigger)
    }

    override suspend fun clearAll() {
        triggerAppDao.clearAll()
    }

    private fun TriggerAppEntity.toDomain(): TriggerApp = TriggerApp(
        packageName = packageName,
        appName = appName,
        isTrigger = isTrigger,
        category = AppCategory.fromString(category)
    )

    private fun TriggerApp.toEntity(): TriggerAppEntity = TriggerAppEntity(
        packageName = packageName,
        appName = appName,
        isTrigger = isTrigger,
        category = category.value
    )
}
