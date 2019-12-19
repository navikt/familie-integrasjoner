package no.nav.familie.integrasjoner.dokarkiv.client.domene

import javax.validation.constraints.NotNull

class Bruker(val idType: @NotNull(message = "Bruker mangler idType") IdType?,
             val id: @NotNull(message = "Bruker mangler id") String?)