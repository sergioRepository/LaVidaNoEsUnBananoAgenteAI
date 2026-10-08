package com.lavidanoesunbanano.domain.crisis

import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Filtro local de detección de crisis y prevención de daño.
 * Dominio puro de Kotlin sin dependencias del framework de Android.
 */
@Singleton
class CrisisFilter @Inject constructor() {

    companion object {
        val DEFAULT_KEYWORDS = listOf(
            "suicidio",
            "suicidarme",
            "matarme",
            "no quiero vivir",
            "hacerme daño",
            "quitarme la vida",
            "acabar con todo"
        )
    }

    /**
     * Normaliza el texto: minúsculas, remueve marcas diacríticas/tildes y colapsa espacios múltiples.
     */
    fun normalize(text: String): String {
        val nfd = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
        val withoutAccents = nfd.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return withoutAccents.replace("\\s+".toRegex(), " ").trim()
    }

    /**
     * Evalúa si el texto ingresado contiene alguna expresión o palabra clave de crisis.
     */
    fun containsCrisisKeywords(
        rawText: String?,
        keywords: List<String> = DEFAULT_KEYWORDS
    ): Boolean {
        if (rawText.isNullOrBlank()) return false

        val normalizedInput = normalize(rawText)
        return keywords.any { keyword ->
            val normalizedKeyword = normalize(keyword)
            normalizedInput.contains(normalizedKeyword)
        }
    }
}
