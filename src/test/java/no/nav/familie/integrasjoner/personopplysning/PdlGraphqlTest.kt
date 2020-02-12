package no.nav.familie.integrasjoner.personopplysning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.familie.integrasjoner.personopplysning.internal.PdlFødselsdatoResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class PdlGraphqlTest {

    private val mapper = ObjectMapper()
            .registerKotlinModule()

    @Test
    fun testSerializationAndDeserialization() {
        val resp = mapper.readValue(File(getFile()), PdlFødselsdatoResponse::class.java)
        assertThat(resp.data!!.person.foedsel[0].foedselsdato).isEqualTo("1955-09-13")
        assertThat(mapper.writeValueAsString (resp.data!!.person.foedsel[0])).contains("fødselsdato")
    }

    private fun getFile(): String {
        return javaClass.classLoader?.getResource("pdl/pdlOkResponse.json")?.file ?: error("Testkonfigurasjon feil")
    }
}