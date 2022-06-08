package no.nav.familie.integrasjoner.adramatch

import no.nav.familie.kontrakter.felles.Fil
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/adramatch/avstemming"])
@Profile("!e2e")
class FiloverføringAdraMatchController(private val sftpClient: FiloverføringAdraMatchClient) {

    @PutMapping
    @ProtectedWithClaims(issuer = "azuread")
    fun lastOppFil(@RequestBody fil: Fil): Ressurs<String> {
        sftpClient.put(fil)
        return Ressurs.success("Fil lastet opp!")
    }
}
