package no.nav.familie.integrasjoner.azure.domene

import java.util.UUID

data class AzureAdBruker(
    val id: UUID,
    val onPremisesSamAccountName: String,
    val userPrincipalName: String,
    val givenName: String,
    val surname: String,
    val streetAddress: String,
    val city: String,
)

data class AzureAdSaksbehandler(
    val azureId: UUID,
    val navIdent: String,
    val fornavn: String,
    val etternavn: String,
    val enhet: String,
    val geografiskEnhet: String
)
