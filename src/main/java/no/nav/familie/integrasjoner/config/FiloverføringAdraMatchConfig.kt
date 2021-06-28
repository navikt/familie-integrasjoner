package no.nav.familie.integrasjoner.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.util.Base64

@ConfigurationProperties("sftp")
@ConstructorBinding
class FiloverføringAdraMatchConfig(val username: String,
                                   val host: String,
                                   val port: Int,
                                   private val privateKey: String,
                                   val passphrase: String) {

    val privateKeyDecoded = base64Decode(privateKey)

    private fun base64Decode(encoded: String): ByteArray {
        return Base64.getDecoder().decode(encoded)
    }

    companion object {

        const val JSCH_CHANNEL_TYPE_SFTP = "sftp"
    }
}