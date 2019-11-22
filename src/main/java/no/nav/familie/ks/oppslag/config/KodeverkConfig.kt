package no.nav.familie.ks.oppslag.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.net.URI

@ConfigurationProperties
@ConstructorBinding
data class KodeverkConfig(val KODEVERK_URL: String, val CREDENTIAL_USERNAME: String) {

    val postnummerUri= URI(KODEVERK_URL + PATH_POSTNUMMER)
    val consumer = CREDENTIAL_USERNAME

    companion object {
        private const val PATH_POSTNUMMER = "/Postnummer/koder/betydninger?ekskluderUgyldige=true&spraak=nb"
    }
}
