package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.integrasjoner.config.incrementLoggFeil
import no.nav.familie.kontrakter.felles.organisasjon.OrganisasjonAdresse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class OrganisasjonRestClient(
    @Value("\${ORGANISASJON_URL}") private val organisasjonUri: URI,
    @Qualifier("noAuthorize") private val restTemplate: RestOperations,
) : AbstractPingableRestClient(restTemplate, "organisasjon") {
    override val pingUri: URI =
        UriComponentsBuilder
            .fromUri(organisasjonUri)
            .pathSegment("ping")
            .build()
            .toUri()

    private fun nøkkelinfoUri(orgnr: String) =
        UriComponentsBuilder
            .fromUri(organisasjonUri)
            .pathSegment("v2/organisasjon", orgnr, "noekkelinfo")
            .build()
            .toUri()

    fun hentOrganisasjon(orgnr: String): HentOrganisasjonResponse =
        try {
            getForEntity(nøkkelinfoUri(orgnr))
        } catch (e: Exception) {
            incrementLoggFeil("organisasjon.hent")
            throw e
        }
}

data class HentOrganisasjonResponse(
    val navn: Navn,
    val adresse: OrganisasjonAdresse?,
)

data class Navn(
    val sammensattnavn: String,
)
