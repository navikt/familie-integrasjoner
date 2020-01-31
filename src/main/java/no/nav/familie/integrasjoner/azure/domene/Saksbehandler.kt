package no.nav.familie.integrasjoner.azure.domene

import java.util.*

class Saksbehandler(val userPrincipalName: String?,
                    val onPremisesSamAccountName: String?,
                    val grupper: List<Gruppe> = LinkedList())
