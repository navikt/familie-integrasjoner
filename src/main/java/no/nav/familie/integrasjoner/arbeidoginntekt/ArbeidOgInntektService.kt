package no.nav.familie.integrasjoner.arbeidoginntekt

import no.nav.familie.integrasjoner.client.rest.ArbeidOgInntektClient
import org.springframework.stereotype.Service

@Service
class ArbeidOgInntektService(
    private val arbeidOgInntektClient: ArbeidOgInntektClient,
) {
    fun hentArbeidOgInntektUrl(
        personIdent: String,
    ): String = arbeidOgInntektClient.hentUrlTilArbeidOgInntekt(personIdent)
}
