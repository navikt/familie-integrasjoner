package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.aktør.internal.AktørResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class AktørregisterRestClient(@Value("\${AKTOERID_URL}")
                              private val aktørRegisterUrl: URI,
                              @Qualifier("sts") restOperations: RestOperations)
    : AbstractPingableRestClient(restOperations, "aktoer") {

    override val pingUri: URI = UriUtil.uri(aktørRegisterUrl, PATH_PING)
    private val hentAktørIdUrl = UriUtil.uri(aktørRegisterUrl, String.format(PATH_HENT_AKTØR_ID, AKTOERID_IDENTGRUPPE))
    private val hentPersonIdentUrl = UriUtil.uri(aktørRegisterUrl, String.format(PATH_HENT_PERSONIDENT, PERSONIDENT_IDENTGRUPPE))

    fun hentAktørId(personIdent: String): AktørResponse {
        return getForEntity(hentAktørIdUrl, httpHeaders(personIdent))
    }

    fun hentPersonIdent(personIdent: String): AktørResponse {
        return getForEntity(hentPersonIdentUrl, httpHeaders(personIdent))
    }

    private fun httpHeaders(personIdent: String): HttpHeaders {
        return HttpHeaders().apply {
            add(NAV_PERSONIDENTER, personIdent)
        }
    }

    companion object {
        private const val NAV_PERSONIDENTER = "Nav-Personidenter"
        private const val AKTOERID_IDENTGRUPPE = "AktoerId"
        private const val PERSONIDENT_IDENTGRUPPE = "NorskIdent"
        private const val PATH_PING = "internal/isAlive"
        private const val PATH_HENT_AKTØR_ID = "api/v1/identer?gjeldende=true&identgruppe=%s"
        private const val PATH_HENT_PERSONIDENT = "api/v1/identer?gjeldende=true&identgruppe=%s"
    }
}
