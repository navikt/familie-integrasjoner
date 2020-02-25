package no.nav.familie.integrasjoner.egenansatt

import no.nav.familie.integrasjoner.client.soap.EgenAnsattSoapClient
import org.springframework.stereotype.Service

@Service
class EgenAnsattService(private val egenAnsattSoapClient: EgenAnsattSoapClient) {

    fun erEgenAnsatt(fnr: String?): Boolean {
        return egenAnsattSoapClient.erEgenAnsatt(fnr)
    }

}