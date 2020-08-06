package no.nav.familie.integrasjoner.aareg

import no.nav.familie.integrasjoner.aareg.domene.Arbeidsforhold
import no.nav.familie.integrasjoner.client.rest.AaregRestClient
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AaregService(private val aaregRestClient: AaregRestClient) {
    fun hentArbeidsforhold(personIdent: String, ansettelsesperiodeFom: LocalDate): List<Arbeidsforhold> {
            return aaregRestClient.hentArbeidsforhold(personIdent, ansettelsesperiodeFom)
    }
}
