package no.nav.familie.integrasjoner.medlemskap

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.integrasjoner.medlemskap.internal.MedlClient
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.io.File
import java.io.IOException
import java.util.*

@Configuration
class MedlemskapTestConfig {
    @Bean
    @Profile("mock-medlemskap")
    @Primary
    @Throws(
            Exception::class) fun medlClientMock(): MedlClient {
        val medlMock = Mockito.mock(MedlClient::class.java)
        Mockito.`when`(medlMock.hentMedlemskapsUnntakResponse(ArgumentMatchers.anyString()))
                .thenReturn(mockMedlemskapResponse())
        Mockito.doNothing().`when`(medlMock).ping()
        return medlMock
    }

    private fun mockMedlemskapResponse(): List<MedlemskapsUnntakResponse> {
        val medlemskapsResponseBody = File(file)
        return try {
            Arrays.asList(*mapper.readValue(
                    medlemskapsResponseBody,
                    Array<MedlemskapsUnntakResponse>::class.java))
        } catch (e: IOException) {
            throw RuntimeException("Feil ved mapping av medl2-mock", e)
        }
    }

    private val file: String
        private get() = javaClass.classLoader.getResource("medlemskap/medlrespons.json").file

    companion object {
        private val mapper = ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}