package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
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
    private val hentAktørIdUrl = UriUtil.uri(aktørRegisterUrl, PATH_HENT, String.format(QUERY_PARAMS, AKTOERID_IDENTGRUPPE))
    private val hentPersonIdentUrl = UriUtil.uri(aktørRegisterUrl, PATH_HENT, String.format(QUERY_PARAMS, PERSONIDENT_IDENTGRUPPE))

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
        private const val PATH_HENT = "api/v1/identer"
        private const val QUERY_PARAMS = "gjeldende=true&identgruppe=%s"
    }
}
