package no.nav.familie.integrasjoner.tilgangskontroll

import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedAdresseBeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedRelasjoner
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

internal class TilgangskontrollUtilTest{

    @Test fun `høyesteGraderingen skal returnere høyeste gradering fra barn`(){
        val person = lagPersonMedRelasjoner(
            adressebeskyttelse = ADRESSEBESKYTTELSEGRADERING.UGRADERT,
            barn = ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG
        )

        assertThat(TilgangskontrollUtil.høyesteGraderingen(person)).isEqualTo(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG)
    }

    @Test fun `høyesteGraderingen skal returnere høyeste gradering fra hovedperson`(){
        val person = lagPersonMedRelasjoner(
            adressebeskyttelse = ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG,
            barn = ADRESSEBESKYTTELSEGRADERING.UGRADERT
        )

        assertThat(TilgangskontrollUtil.høyesteGraderingen(person)).isEqualTo(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG)
    }

    @Test fun `høyesteGraderingen skal returnere ugradert `(){
        val person = lagPersonMedRelasjoner(
            adressebeskyttelse = null,
            barn = ADRESSEBESKYTTELSEGRADERING.UGRADERT
        )

        assertThat(TilgangskontrollUtil.høyesteGraderingen(person)).isEqualTo(ADRESSEBESKYTTELSEGRADERING.UGRADERT)
    }

    @Test fun `høyesteGraderingen skal returnere null `(){
        val person = lagPersonMedRelasjoner(
            adressebeskyttelse = null,
            barn = null
        )

        assertThat(TilgangskontrollUtil.høyesteGraderingen(person)).isEqualTo(null)
    }

    @Test fun `høyesteGraderingen skal returnere strengeste gradering hvis mange `(){
        val person = lagPersonMedRelasjoner(
            adressebeskyttelse = null,
            barn = ADRESSEBESKYTTELSEGRADERING.UGRADERT,
            sivilstand = ADRESSEBESKYTTELSEGRADERING.FORTROLIG,
            fullmakt = ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG,
            barnsForeldrer = ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG_UTLAND
        )

        assertThat(TilgangskontrollUtil.høyesteGraderingen(person)).isEqualTo(ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG_UTLAND)
    }

    private fun lagPersonMedRelasjoner(adressebeskyttelse: ADRESSEBESKYTTELSEGRADERING? = null,
                                       sivilstand: ADRESSEBESKYTTELSEGRADERING? = null,
                                       fullmakt: ADRESSEBESKYTTELSEGRADERING? = null,
                                       barn: ADRESSEBESKYTTELSEGRADERING? = null,
                                       barnsForeldrer: ADRESSEBESKYTTELSEGRADERING? = null): PersonMedRelasjoner {
        return PersonMedRelasjoner(personIdent = "",
                                   adressebeskyttelse = adressebeskyttelse,
                                   sivilstand = lagPersonMedBeskyttelse(sivilstand, "sivilstand"),
                                   fullmakt = lagPersonMedBeskyttelse(fullmakt, "fullmakt"),
                                   barn = lagPersonMedBeskyttelse(barn, "barn"),
                                   barnsForeldrer = lagPersonMedBeskyttelse(barnsForeldrer, "barnsForeldrer"))
    }

    private fun lagPersonMedBeskyttelse(sivilstand: ADRESSEBESKYTTELSEGRADERING?, personIdent: String) =
        sivilstand?.let { listOf(PersonMedAdresseBeskyttelse(personIdent, it)) } ?: emptyList()
}