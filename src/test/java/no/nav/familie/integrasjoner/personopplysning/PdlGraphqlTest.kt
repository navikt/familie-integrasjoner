package no.nav.familie.integrasjoner.personopplysning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.familie.integrasjoner.personopplysning.internal.PdlHentPersonResponse
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class PdlGraphqlTest {

    private val mapper = ObjectMapper()
            .registerKotlinModule()

    @Test
    fun testSerializationAndDeserialization() {
        val resp = mapper.readValue(File(getFile("pdl/pdlOkResponse.json")), PdlHentPersonResponse::class.java)
        assertThat(resp.data!!.person!!.foedsel[0].foedselsdato).isEqualTo("1955-09-13")
        assertThat(mapper.writeValueAsString (Person(resp.data!!.person!!.foedsel[0].foedselsdato!!))).contains("fødselsdato")
    }

    @Test
    fun testDeserializationOfResponseWithErrors() {
        val resp = mapper.readValue(File(getFile("pdl/pdlPersonIkkeFunnetResponse.json")), PdlHentPersonResponse::class.java)
        assertThat(resp.harFeil()).isTrue()
    }

    @Test
    fun testDeserializationOfResponseWithoutFødselsdato() {
        val resp = mapper.readValue(File(getFile("pdl/pdlManglerFoedselResponse.json")), PdlHentPersonResponse::class.java)
        assertThat(resp.data!!.person!!.foedsel[0].foedselsdato).isNull()
    }

    private fun getFile(name: String): String {
        return javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")
    }
}