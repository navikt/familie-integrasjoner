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
import no.nav.familie.integrasjoner.personopplysning.internal.PdlResponse
import no.nav.familie.kontrakter.felles.personopplysning.SIVILSTAND
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class PdlGraphqlTest {

    private val mapper = ObjectMapper()
            .registerKotlinModule()

    @Test
    fun testDeserialization() {
        val resp: PdlResponse<PdlPerson> = mapper.readValue(File(getFile("pdl/pdlOkResponse.json")))
        assertThat(resp.data.person!!.foedsel.first().foedselsdato).isEqualTo("1955-09-13")
        assertThat(resp.data.person!!.navn.first().fornavn).isEqualTo("ENGASJERT")
        assertThat(resp.data.person!!.kjoenn.first().kjoenn.toString()).isEqualTo("MANN")
        assertThat(resp.data.person!!.familierelasjoner.first().relatertPersonsIdent).isEqualTo("12345678910")
        assertThat(resp.data.person!!.familierelasjoner.first().relatertPersonsRolle.toString()).isEqualTo("BARN")
        assertThat(resp.data.person!!.sivilstand.first()!!.type).isEqualTo(SIVILSTAND.UGIFT)
        assertThat(resp.data.person!!.bostedsadresse.first()?.vegadresse?.husnummer).isEqualTo("3")
        assertThat(resp.data.person!!.bostedsadresse.first()?.vegadresse?.matrikkelId).isEqualTo(1234)
        assertNull(resp.data.person!!.bostedsadresse.first()?.matrikkeladresse)
        assertNull(resp.data.person!!.bostedsadresse.first()?.ukjentBosted)
        assertThat(resp.errorMessages()).isEqualTo("")
    }

    @Test
    fun testTomAdresse() {
        val resp: PdlResponse<PdlPerson> = mapper.readValue(File(getFile("pdl/pdlTomAdresseOkResponse.json")))
        assertTrue(resp.data.person!!.bostedsadresse.isEmpty())
    }

    @Test
    fun testMatrikkelAdresse() {
        val resp: PdlResponse<PdlPerson> = mapper.readValue(File(getFile("pdl/pdlMatrikkelAdresseOkResponse.json")))
        assertThat(resp.data.person!!.bostedsadresse.first()?.matrikkeladresse?.postnummer).isEqualTo("0274")
        assertThat(resp.data.person!!.bostedsadresse.first()?.matrikkeladresse?.matrikkelId).isEqualTo(2147483649)
    }

    @Test
    fun testUkjentBostedAdresse() {
        val resp: PdlResponse<PdlPerson> = mapper.readValue(File(getFile("pdl/pdlUkjentBostedAdresseOkResponse.json")))
        assertThat(resp.data.person!!.bostedsadresse.first()?.ukjentBosted?.bostedskommune).isEqualTo("Oslo")
    }

    @Test
    fun testDeserializationOfResponseWithErrors() {
        val resp: PdlResponse<PdlPerson> = mapper.readValue(File(getFile("pdl/pdlPersonIkkeFunnetResponse.json")))
        assertThat(resp.harFeil()).isTrue()
        assertThat(resp.errorMessages()).contains("Fant ikke person", "Ikke tilgang")
    }

    @Test
    fun testDeserializationOfResponseWithoutFødselsdato() {
        val resp: PdlResponse<PdlPerson> = mapper.readValue(File(getFile("pdl/pdlManglerFoedselResponse.json")))
        assertThat(resp.data.person!!.foedsel.first().foedselsdato).isNull()
    }

    @Test
    fun testPdlIdenter() {
        val resp: PdlResponse<PdlHentIdenter> = mapper.readValue(File(getFile("pdl/pdlIdenterResponse.json")))
        assertThat(resp.data.hentIdenter!!.identer).hasSize(1)
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
        val pdlDto = GeografiskTilknytningDto(gtType = GeografiskTilknytningType.KOMMUNE,
                                              gtKommune = "0301",
                                              gtBydel = null,
                                              gtLand = null)

        val resp: PdlResponse<PdlHentGeografiskTilknytning> =
                mapper.readValue(File(getFile("pdl/pdlGeografiskTilknytningResponse.json")))

        assertThat(resp.harFeil()).isFalse
        assertThat(resp.data.hentGeografiskTilknytning).usingRecursiveComparison().isEqualTo(pdlDto)
    }

    private fun getFile(name: String): String {
        return javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")
    }

}