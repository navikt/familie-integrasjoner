package no.nav.familie.integrasjoner.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import java.util.Base64

@ConfigurationProperties("sftp")
@Profile("!gcp")
class FiloverføringAdraMatchConfig(
    val username: String,
    val host: String,
    val port: Int,
    privateKey: String,
    val passphrase: String,
    val directory: String = "inbound",
) {
    val privateKeyDecoded = base64Decode(privateKey)

    private fun base64Decode(encoded: String): ByteArray = Base64.getDecoder().decode(encoded)

    companion object {
        const val JSCH_CHANNEL_TYPE_SFTP = "sftp"
    }
}
