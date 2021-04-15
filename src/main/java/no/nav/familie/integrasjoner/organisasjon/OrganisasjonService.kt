package no.nav.familie.integrasjoner.organisasjon

import no.nav.familie.integrasjoner.client.soap.OrganisasjonSoapClient
import no.nav.familie.kontrakter.felles.organisasjon.Organisasjon
import no.nav.tjeneste.virksomhet.organisasjon.v5.informasjon.UstrukturertNavn
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonRequest
import org.springframework.stereotype.Service

@Service
class OrganisasjonService(private val organisasjonSoapClient: OrganisasjonSoapClient) {


    fun hentOrganisasjon(orgnr: String): Organisasjon {
        val hentOrganisasjonRequest = HentOrganisasjonRequest().apply {
            orgnummer = orgnr
            isInkluderHistorikk = false
            isInkluderHierarki = false
            isInkluderAnsatte = false
        }
        val organisasjonResponse = organisasjonSoapClient.hentOrganisasjon(hentOrganisasjonRequest)
        val navnelinjer = (organisasjonResponse.organisasjon.navn as UstrukturertNavn).navnelinje
        val navn = navnelinjer.joinToString(" ").trim()

        return Organisasjon(orgnr, navn)

    }
}