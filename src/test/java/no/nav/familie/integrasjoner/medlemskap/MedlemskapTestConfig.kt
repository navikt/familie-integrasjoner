package no.nav.familie.integrasjoner.medlemskap

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.MedlRestClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.io.File
import java.io.IOException

@Configuration
class MedlemskapTestConfig {
    @Bean
    @Profile("mock-medlemskap")
    @Primary
    @Throws(Exception::class)
    fun medlClientMock(): MedlRestClient {
        val medlMock: MedlRestClient = mockk(relaxed = true)
        every { medlMock.hentMedlemskapsUnntakResponse(any()) }
            .returns(medlemskapResponse())
        return medlMock
    }

    private fun medlemskapResponse(): List<MedlemskapsunntakResponse> {
        val medlemskapResponseBody = File(file)
        return try {
            listOf(
                *mapper.readValue(
                    medlemskapResponseBody,
                    Array<MedlemskapsunntakResponse>::class.java,
                ),
            )
        } catch (e: IOException) {
            throw RuntimeException("Feil ved mapping av medl2-mock", e)
        }
    }

    private val file: String = javaClass.classLoader.getResource("medlemskap/medlrespons.json").file

    companion object {
        private val mapper =
            ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerKotlinModule()
                .registerModule(JavaTimeModule())
    }
}
