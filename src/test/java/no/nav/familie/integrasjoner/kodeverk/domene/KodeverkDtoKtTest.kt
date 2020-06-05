package no.nav.familie.integrasjoner.kodeverk.domene

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

class KodeverkDtoKtTest {

    @Test
    internal fun `KodeverkDto mapTerm mapper nykkel til term`() {
        val betydninger = mapOf(
                "NOR" to listOf(BetydningDto("",
                                             "",
                                             mapOf("nb" to BeskrivelseDto("NorgeTerm",
                                                                          "NorgeTekst")))),
                "SWE" to listOf(BetydningDto("",
                                             "",
                                             mapOf())))
        val kodeverkDto = KodeverkDto(betydninger)

        assertThat(kodeverkDto.mapTerm())
                .isEqualTo(mapOf("NOR" to "NorgeTerm",
                                 "SWE" to ""))
    }

    @Test
    internal fun `KodeverkDto mapTerm skal feile når det finnes historikk`() {
        val betydninger = mapOf(
                "NOR" to listOf(BetydningDto(LocalDate.of(2000, 1, 1).toString(),
                                             LocalDate.of(2010, 1, 1).toString(),
                                             mapOf("nb" to BeskrivelseDto("NorgeTerm",
                                                                          "NorgeTekst"))),
                                BetydningDto(LocalDate.of(2010, 1, 2).toString(),
                                             LocalDate.of(2099, 1, 2).toString(),
                                             mapOf("nb" to BeskrivelseDto("NorgeTerm",
                                                                          "NorgeTekst"))))
        )
        val kodeverkDto = KodeverkDto(betydninger)
        assertThatThrownBy { kodeverkDto.mapTerm() }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("List has more than one element.")
    }
}