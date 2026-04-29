package no.nav.familie.integrasjoner.organisasjon

import no.nav.familie.integrasjoner.client.rest.OrganisasjonRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.organisasjon.Organisasjon
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class OrganisasjonService(
    private val organisasjonRestClient: OrganisasjonRestClient,
) {
    @Cacheable("hentOrganisasjon")
    fun hentOrganisasjon(orgnr: String): Organisasjon {
        val organisasjonResponse = organisasjonRestClient.hentOrganisasjon(orgnr)
        return Organisasjon(orgnr, organisasjonResponse.navn.sammensattnavn, organisasjonResponse.adresse)
    }

    @Cacheable("validerOrganisasjon")
    fun validerOrganisasjon(orgnr: String): Boolean =
        try {
            organisasjonRestClient.hentOrganisasjon(orgnr)
            true
        } catch (oppslagException: OppslagException) {
            false
        }
}
