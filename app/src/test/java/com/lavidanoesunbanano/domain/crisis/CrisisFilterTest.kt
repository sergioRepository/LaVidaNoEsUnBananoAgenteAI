package com.lavidanoesunbanano.domain.crisis

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CrisisFilterTest {

    private lateinit var filter: CrisisFilter

    @Before
    fun setup() {
        filter = CrisisFilter()
    }

    @Test
    fun containsCrisisKeywords_exactMatchWithoutAccents_returnsTrue() {
        assertTrue(filter.containsCrisisKeywords("suicidio"))
        assertTrue(filter.containsCrisisKeywords("matarme"))
    }

    @Test
    fun containsCrisisKeywords_withAccentsAndDiacritics_returnsTrue() {
        // "hacerme daño" con y sin tildes/eñes o variaciones
        assertTrue(filter.containsCrisisKeywords("hacerme daño"))
        assertTrue(filter.containsCrisisKeywords("hácerme daño"))
        assertTrue(filter.containsCrisisKeywords("suicídio"))
    }

    @Test
    fun containsCrisisKeywords_inUppercase_returnsTrue() {
        assertTrue(filter.containsCrisisKeywords("SUICIDARME"))
        assertTrue(filter.containsCrisisKeywords("NO QUIERO VIVIR"))
        assertTrue(filter.containsCrisisKeywords("ACABAR CON TODO"))
    }

    @Test
    fun containsCrisisKeywords_embeddedInsideLongText_returnsTrue() {
        val longText = "Hola a todos, hoy me siento realmente mal, siento que ya no doy más y creo que quiero quitarme la vida porque no encuentro salida."
        assertTrue(filter.containsCrisisKeywords(longText))
    }

    @Test
    fun containsCrisisKeywords_withMultipleSpacesAndTabs_returnsTrue() {
        val spacingText = "siento   que  quiero     matarme     hoy"
        assertTrue(filter.containsCrisisKeywords(spacingText))
    }

    @Test
    fun containsCrisisKeywords_normalText_returnsFalse() {
        assertFalse(filter.containsCrisisKeywords("Solo estoy cansado de trabajar todo el día y quiero relajarme."))
        assertFalse(filter.containsCrisisKeywords("Me aburre esta serie de televisión."))
        assertFalse(filter.containsCrisisKeywords("Hola mundo, todo está bien."))
        assertFalse(filter.containsCrisisKeywords(""))
        assertFalse(filter.containsCrisisKeywords(null))
    }
}
