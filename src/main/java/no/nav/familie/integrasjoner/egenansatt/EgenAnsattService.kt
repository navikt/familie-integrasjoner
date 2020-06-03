package no.nav.familie.integrasjoner.egenansatt

import no.nav.familie.integrasjoner.client.soap.EgenAnsattSoapClient
import no.nav.familie.integrasjoner.tilgangskontroll.TilgangskontrollService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class EgenAnsattService(private val egenAnsattSoapClient: EgenAnsattSoapClient) {

    @Cacheable("erEgenAnsatt",
               key = "#fnr",
               condition = "#fnr != null")
    fun erEgenAnsatt(fnr: String?): Boolean {
        return egenAnsattSoapClient.erEgenAnsatt(fnr)
    }

}