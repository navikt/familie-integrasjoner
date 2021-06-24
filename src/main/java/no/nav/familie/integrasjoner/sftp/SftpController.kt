package no.nav.familie.integrasjoner.sftp

import no.nav.familie.kontrakter.felles.Fil
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/sftp"])
@Profile("!e2e")
class SftpController(private val sftpClient: SftpClient) {

    @PutMapping
    @ProtectedWithClaims(issuer = "azuread")
    fun lastOppFil(@RequestBody fil: Fil): Ressurs<String> {
        sftpClient.put(fil)
        return Ressurs.success("Fil lastet opp!")
    }

}
