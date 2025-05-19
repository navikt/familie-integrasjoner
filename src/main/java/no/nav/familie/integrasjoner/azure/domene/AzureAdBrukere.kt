package no.nav.familie.integrasjoner.azure.domene

data class AzureAdBrukere(
    val value: List<AzureAdBruker>,
)

data class AzureAdGruppe(
    val id: String,
    val displayName: String?,
)

data class AzureAdGrupper(
    val value: List<AzureAdGruppe>,
)
