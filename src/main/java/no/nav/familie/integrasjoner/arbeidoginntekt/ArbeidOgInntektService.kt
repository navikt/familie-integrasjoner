package no.nav.familie.integrasjoner.arbeidoginntekt

import no.nav.familie.integrasjoner.client.rest.ArbeidOgInntektClient
import no.nav.familie.kontrakter.felles.PersonIdent
import org.springframework.stereotype.Service

@Service
class ArbeidOgInntektService(
    private val arbeidOgInntektClient: ArbeidOgInntektClient,
) {
    fun hentArbeidOgInntektUrl(
        personIdent: PersonIdent,
    ): String = arbeidOgInntektClient.hentUrlTilArbeidOgInntekt(personIdent)
}
