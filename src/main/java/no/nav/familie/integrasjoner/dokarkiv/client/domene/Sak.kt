package no.nav.familie.integrasjoner.dokarkiv.client.domene

import javax.validation.constraints.NotNull

class Sak {
    private val arkivsaksnummer: @NotNull(message = "Sak mangler arkivsaksnummer") String? =
            null
    private val arkivsaksystem: @NotNull(message = "Sak mangler arkivsaksystem") String? =
            null
}