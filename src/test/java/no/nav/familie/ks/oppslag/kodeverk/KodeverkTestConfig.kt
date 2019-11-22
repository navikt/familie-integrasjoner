package no.nav.familie.ks.oppslag.kodeverk

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.ef.mottak.api.kodeverk.KodeverkClient
import no.nav.familie.ef.mottak.api.kodeverk.domene.PostnummerDto
import org.mockito.Mockito
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
    @Throws(Exception::class) fun kodeverkClientMock(): KodeverkClient? {
        val kodeverkClient = Mockito.mock(KodeverkClient::class.java)
        Mockito.`when`(kodeverkClient.hentPostnummerBetydninger()).thenReturn(mockPostnummerRespons())
        Mockito.doNothing().`when`(kodeverkClient).ping()
        return kodeverkClient
    }

    private fun mockPostnummerRespons(): PostnummerDto {
        val postnummerResponseBody = File(getFile())
        return try {
            mapper.readValue(postnummerResponseBody, PostnummerDto::class.java)
        } catch (e: IOException) {
            throw RuntimeException("Feil ved mapping av postnummerMock", e)
        }
    }

    private fun getFile(): String? {
        return javaClass.classLoader.getResource("kodeverk/postnummerrespons.json").file
    }
}
