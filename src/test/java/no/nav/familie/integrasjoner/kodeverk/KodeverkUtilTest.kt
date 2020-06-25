package no.nav.familie.integrasjoner.kodeverk

import no.nav.familie.kontrakter.felles.kodeverk.BeskrivelseDto
import no.nav.familie.kontrakter.felles.kodeverk.BetydningDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDate.now

class KodeverkUtilTest {

    @Test
    internal fun `KodeverkDto mapTerm mapper nykkel til term`() {
        val betydninger = mapOf(
                "NOR" to listOf(BetydningDto(now(),
                                             now(),
                                             mapOf("nb" to BeskrivelseDto("NorgeTerm",
                                                                          "NorgeTekst")))),
                "SWE" to listOf(BetydningDto(now(),
                                             now(),
                                             mapOf())))
        val kodeverkDto = KodeverkDto(betydninger)

        assertThat(kodeverkDto.mapTerm())
                .isEqualTo(mapOf("NOR" to "NorgeTerm",
                                 "SWE" to ""))
    }

    @Test
    internal fun `KodeverkDto mapTerm henter gjeldende når det finnes historikk`() {
        val betydninger = mapOf(
                "NOR" to listOf(BetydningDto(LocalDate.of(2000, 1, 1),
                                             LocalDate.of(2010, 1, 1),
                                             mapOf("nb" to BeskrivelseDto("IkkeGjeldende",
                                                                          "IkkeGjeldende"))),
                                BetydningDto(LocalDate.of(2010, 1, 2),
                                             LocalDate.of(2099, 1, 2),
                                             mapOf("nb" to BeskrivelseDto("Gjeldende",
                                                                          "Gjeldende"))))
        )
        val kodeverkDto = KodeverkDto(betydninger)
        assertThat(kodeverkDto.mapTerm().get("NOR"))
                .isEqualTo("Gjeldende")
    }

}