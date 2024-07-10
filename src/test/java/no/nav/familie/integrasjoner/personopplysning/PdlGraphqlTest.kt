package no.nav.familie.integrasjoner.personopplysning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningDto
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningType
import no.nav.familie.integrasjoner.geografisktilknytning.PdlHentGeografiskTilknytning
import no.nav.familie.integrasjoner.personopplysning.internal.PdlHentIdenter
import no.nav.familie.integrasjoner.personopplysning.internal.PdlNavn
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPerson
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonMedAdressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.PdlResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class PdlGraphqlTest {
    private val mapper =
        ObjectMapper()
            .registerKotlinModule()

    @Test
    fun testDeserialization() {
        val resp: PdlResponse<PdlPerson> = mapper.readValue(File(getFile("pdl/pdlOkResponseEnkel.json")))
        assertThat(
            resp.data.person!!
                .navn
                .first()
                .fornavn,
        ).isEqualTo("ENGASJERT")
        assertThat(resp.errorMessages()).isEqualTo("")
    }

    @Test
    fun testDeserializationOfResponseWithErrors() {
        val resp: PdlResponse<PdlPerson> = mapper.readValue(File(getFile("pdl/pdlPersonIkkeFunnetResponse.json")))
        assertThat(resp.harFeil()).isTrue()
        assertThat(resp.errorMessages()).contains("Fant ikke person", "Ikke tilgang")
    }

    @Test
    fun testPdlIdenter() {
        val resp: PdlResponse<PdlHentIdenter> = mapper.readValue(File(getFile("pdl/pdlIdenterResponse.json")))
        assertThat(resp.data.hentIdenter!!.identer).hasSize(1)
    }

    @Test
    fun testPdlAdressebeskyttelse() {
        val resp: PdlResponse<PdlPersonMedAdressebeskyttelse> = mapper.readValue(File(getFile("pdl/pdlAdressebeskyttelseResponse.json")))
        assertThat(resp.data.person?.adressebeskyttelse).hasSize(1)
    }

    @Test
    fun testFulltNavn() {
        assertThat(PdlNavn(fornavn = "For", mellomnavn = "Mellom", etternavn = "Etter").fulltNavn())
            .isEqualTo("For Mellom Etter")
        assertThat(PdlNavn(fornavn = "For", etternavn = "Etter").fulltNavn())
            .isEqualTo("For Etter")
    }

    @Test
    fun testGeografiskTilknytningMapper() {
        val pdlDto =
            GeografiskTilknytningDto(
                gtType = GeografiskTilknytningType.KOMMUNE,
                gtKommune = "0301",
                gtBydel = null,
                gtLand = null,
            )

        val resp: PdlResponse<PdlHentGeografiskTilknytning> =
            mapper.readValue(File(getFile("pdl/pdlGeografiskTilknytningResponse.json")))

        assertThat(resp.harFeil()).isFalse
        assertThat(resp.data.hentGeografiskTilknytning).usingRecursiveComparison().isEqualTo(pdlDto)
    }

    private fun getFile(name: String): String = javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")
}
