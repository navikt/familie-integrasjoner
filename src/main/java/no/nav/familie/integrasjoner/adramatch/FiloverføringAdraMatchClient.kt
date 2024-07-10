package no.nav.familie.integrasjoner.adramatch

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import no.nav.familie.integrasjoner.config.FiloverføringAdraMatchConfig
import no.nav.familie.kontrakter.felles.Fil
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream

@Service
class FiloverføringAdraMatchClient(
    private val config: FiloverføringAdraMatchConfig,
) {
    private val jSch =
        JSch().apply {
            addIdentity(
                config.username,
                config.privateKeyDecoded,
                null,
                config.passphrase.toByteArray(),
            )
        }

    fun put(fil: Fil) {
        var session: Session? = null
        var channel: ChannelSftp? = null
        try {
            session = jSch.getSession(config.username, config.host, config.port).apply { }
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect(1000)

            channel = session.openChannel(FiloverføringAdraMatchConfig.JSCH_CHANNEL_TYPE_SFTP) as ChannelSftp
            channel.connect()
            channel.cd(config.directory)
            channel.put(ByteArrayInputStream(fil.innhold), fil.navn)
        } finally {
            channel?.disconnect()
            session?.disconnect()
        }
    }
}
