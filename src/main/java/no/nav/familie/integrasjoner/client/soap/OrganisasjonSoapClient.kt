package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.http.client.AbstractSoapClient
import no.nav.familie.http.client.Pingable
import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.OrganisasjonV5
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonRequest
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonResponse
import org.springframework.stereotype.Component

@Component
class OrganisasjonSoapClient(private val organisasjonV5: OrganisasjonV5)
    : AbstractSoapClient("OrganisasjonV5"), Pingable {

    fun hentOrganisasjon(request: HentOrganisasjonRequest): HentOrganisasjonResponse {
        return executeMedMetrics {
            organisasjonV5.hentOrganisasjon(request)
        }
    }

    override fun ping() {
        organisasjonV5.ping()
    }

}