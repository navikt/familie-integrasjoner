package no.nav.familie.integrasjoner.kodeverk

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.KodeverkClient
import no.nav.familie.integrasjoner.kodeverk.domene.KodeverkDto
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.io.File
import java.io.IOException

@ConfigurationProperties
@ConstructorBinding
class KodeverkTestConfig {


    private val mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Bean
    @Profile("mock-kodeverk")
    @Primary
    fun kodeverkClientMock(): KodeverkClient {
        val kodeverkClient: KodeverkClient = mockk()
        every { kodeverkClient.hentPostnummer() } returns mockPostnummerRespons()
        every { kodeverkClient.ping() } answers { nothing }
        return kodeverkClient
    }

    private fun mockPostnummerRespons(): KodeverkDto {
        val postnummerResponseBody = File(getFile())
        return try {
            mapper.readValue(postnummerResponseBody)
        } catch (e: IOException) {
            throw RuntimeException("Feil ved mapping av postnummerMock", e)
        }
    }

    private fun getFile(): String {
        return javaClass.classLoader?.getResource("kodeverk/postnummerrespons.json")?.file ?: error("Testkonfigurasjon feil")
    }
}
