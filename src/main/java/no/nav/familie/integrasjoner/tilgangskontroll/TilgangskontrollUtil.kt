package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG_UTLAND
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedRelasjoner

object TilgangskontrollUtil {
    fun høyesteGraderingen(personUtvidet: PersonMedRelasjoner): ADRESSEBESKYTTELSEGRADERING? {
        val adressebeskyttelser =
            lagListeAvRelasjonersAdressegraderinger(personUtvidet) + personUtvidet.adressebeskyttelse
        return when {
            adressebeskyttelser.contains(STRENGT_FORTROLIG_UTLAND) -> STRENGT_FORTROLIG_UTLAND
            adressebeskyttelser.contains(STRENGT_FORTROLIG) -> STRENGT_FORTROLIG
            adressebeskyttelser.contains(FORTROLIG) -> FORTROLIG
            adressebeskyttelser.contains(ADRESSEBESKYTTELSEGRADERING.UGRADERT) -> ADRESSEBESKYTTELSEGRADERING.UGRADERT
            else -> null
        }
    }

    private fun lagListeAvRelasjonersAdressegraderinger(personUtvidet: PersonMedRelasjoner) =
        listOf(personUtvidet.sivilstand, personUtvidet.fullmakt, personUtvidet.barn, personUtvidet.barnsForeldrer)
            .flatMap { relasjoner -> relasjoner.mapNotNull { it.adressebeskyttelse } }
}
