package no.nav.familie.integrasjoner.personopplysning

import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import java.io.File
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.familie.integrasjoner.personopplysning.internal.PdlHentPersonResponse
import no.nav.familie.integrasjoner.personopplysning.internal.PdlNavn
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PdlGraphqlTest {

    private val mapper = ObjectMapper()
            .registerKotlinModule()

    @Test
    fun testDeserialization() {
        val resp = mapper.readValue(File(getFile("pdl/pdlOkResponse.json")), PdlHentPersonResponse::class.java)
        assertThat(resp.data!!.person!!.foedsel.first().foedselsdato).isEqualTo("1955-09-13")
        assertThat(resp.data!!.person!!.navn.first().fornavn).isEqualTo("ENGASJERT")
        assertThat(resp.data!!.person!!.kjoenn.first().kjoenn.toString()).isEqualTo("MANN")
        assertThat(resp.errorMessages()).isEqualTo("")
    }

    @Test
    fun testDeserializationOfResponseWithErrors() {
        val resp = mapper.readValue(File(getFile("pdl/pdlPersonIkkeFunnetResponse.json")), PdlHentPersonResponse::class.java)
        assertThat(resp.harFeil()).isTrue()
        assertThat(resp.errorMessages()).contains("Fant ikke person", "Ikke tilgang")
    }

    @Test
    fun testDeserializationOfResponseWithoutFødselsdato() {
        val resp = mapper.readValue(File(getFile("pdl/pdlManglerFoedselResponse.json")), PdlHentPersonResponse::class.java)
        assertThat(resp.data!!.person!!.foedsel.first().foedselsdato).isNull()
    }

    @Test
    fun testFulltNavn() {
        assertThat(PdlNavn(fornavn = "For", mellomnavn = "Mellom", etternavn = "Etter").fulltNavn())
                .isEqualTo("For Mellom Etter")
        assertThat(PdlNavn(fornavn = "For", etternavn = "Etter").fulltNavn())
                .isEqualTo("For Etter")
    }

    private fun getFile(name: String): String {
        return javaClass.classLoader?.getResource(name)?.file ?: error("Testkonfigurasjon feil")
    }

    @Test
    fun testreg() {
        //val test = TypeResolver {  };

        val schemaParser = SchemaParser()
        //val registry: TypeDefinitionRegistry = schemaParser.parse(this::class.java.getResource("/pdl/pdl-api-schema.graphqls").readText())
        val registry = schemaParser.parse(this::class.java.getResource("/pdl/pdl-api-schema.graphql").readText())
        print(registry.types())

    }

}