package no.nav.familie.integrasjoner.azure.domene


data class AzureAdBruker(val id: String,
                         val userPrincipalName: String,
                         val givenName: String,
                         val surname: String)
