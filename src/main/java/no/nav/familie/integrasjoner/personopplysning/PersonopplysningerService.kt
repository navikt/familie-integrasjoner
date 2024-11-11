package no.nav.familie.integrasjoner.personopplysning

import no.nav.familie.integrasjoner.client.rest.PdlClientCredentialRestClient
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.client.rest.RegoppslagRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.Adressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.FORELDERBARNRELASJONROLLE
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonMedRelasjonerOgAdressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedAdresseBeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedRelasjoner
import no.nav.familie.integrasjoner.personopplysning.internal.PostadresseResponse
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.personopplysning.FinnPersonidenterResponse
import no.nav.familie.kontrakter.felles.personopplysning.PersonIdentMedHistorikk
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope

@Service
@ApplicationScope
class PersonopplysningerService(
    private val pdlRestClient: PdlRestClient,
    private val pdlClientCredentialRestClient: PdlClientCredentialRestClient,
    private val regoppslagRestClient: RegoppslagRestClient,
) {
    fun hentPersoninfo(
        personIdent: String,
        tema: Tema,
    ): Person = pdlRestClient.hentPerson(personIdent, tema)

    fun hentIdenter(
        personIdent: String,
        tema: Tema,
        medHistorikk: Boolean,
    ): FinnPersonidenterResponse {
        val response = pdlRestClient.hentIdenter(personIdent, "FOLKEREGISTERIDENT", tema, medHistorikk)
        return FinnPersonidenterResponse(response.map { PersonIdentMedHistorikk(it.ident, it.historisk) })
    }

    @Cacheable(cacheNames = ["PERSON_MED_RELASJONER"], key = "#personIdent + #tema", condition = "#personIdent != null")
    fun hentPersonMedRelasjoner(
        personIdent: String,
        tema: Tema,
    ): PersonMedRelasjoner {
        val hovedperson = hentPersonMedRelasjonerOgAdressebeskyttelse(listOf(personIdent), tema).getOrThrow(personIdent)
        val barnIdenter =
            hovedperson.forelderBarnRelasjon
                .filter { it.relatertPersonsRolle == FORELDERBARNRELASJONROLLE.BARN }
                .mapNotNull { it.relatertPersonsIdent }
        val sivilstandIdenter = hovedperson.sivilstand.mapNotNull { it.relatertVedSivilstand }.distinct()

        val tilknyttedeIdenter = (barnIdenter + sivilstandIdenter).distinct()
        val tilknyttedeIdenterData = hentPersonMedRelasjonerOgAdressebeskyttelse(tilknyttedeIdenter, tema)

        val barnOpplysninger = tilknyttedeIdenterData.filter { (ident, _) -> barnIdenter.contains(ident) }
        val barnsForeldrer = hentBarnsForeldrer(barnOpplysninger, personIdent, tema)

        return PersonMedRelasjoner(
            personIdent = personIdent,
            adressebeskyttelse = hovedperson.gradering(),
            sivilstand = mapPersonMedRelasjoner(sivilstandIdenter, tilknyttedeIdenterData),
            barn = mapPersonMedRelasjoner(barnIdenter, tilknyttedeIdenterData),
            barnsForeldrer = barnsForeldrer,
        )
    }

    fun hentAdressebeskyttelse(
        personIdent: String,
        tema: Tema,
    ): Adressebeskyttelse =
        pdlRestClient.hentAdressebeskyttelse(personIdent, tema).adressebeskyttelse.firstOrNull()
            ?: Adressebeskyttelse(gradering = ADRESSEBESKYTTELSEGRADERING.UGRADERT)

    fun hentPostadresse(
        personIdent: String,
        tema: Tema,
    ): PostadresseResponse? = regoppslagRestClient.hentPostadresse(personIdent, tema)

    private fun hentBarnsForeldrer(
        barnOpplysninger: Map<String, PdlPersonMedRelasjonerOgAdressebeskyttelse>,
        personIdent: String,
        tema: Tema,
    ): List<PersonMedAdresseBeskyttelse> {
        val barnsForeldrerIdenter =
            barnOpplysninger
                .flatMap { (_, personMedRelasjoner) ->
                    personMedRelasjoner.forelderBarnRelasjon.filter { it.relatertPersonsRolle != FORELDERBARNRELASJONROLLE.BARN }
                }.filter { it.relatertPersonsIdent != personIdent }
                .mapNotNull { it.relatertPersonsIdent }
                .distinct()
        val barnsForeldrerOpplysninger = hentPersonMedRelasjonerOgAdressebeskyttelse(barnsForeldrerIdenter, tema)

        return mapPersonMedRelasjoner(barnsForeldrerIdenter, barnsForeldrerOpplysninger)
    }

    private fun mapPersonMedRelasjoner(
        sivilstandIdenter: List<String>,
        tilknyttedeIdenterData: Map<String, PdlPersonMedRelasjonerOgAdressebeskyttelse>,
    ) = sivilstandIdenter.map { PersonMedAdresseBeskyttelse(it, tilknyttedeIdenterData.getOrThrow(it).gradering()) }

    private fun PdlPersonMedRelasjonerOgAdressebeskyttelse.gradering() = this.adressebeskyttelse.firstOrNull()?.gradering

    private fun Map<String, PdlPersonMedRelasjonerOgAdressebeskyttelse>.getOrThrow(ident: String) =
        this[ident] ?: throw OppslagException(
            "Finner ikke $ident i response fra pdl",
            "pdl",
            OppslagException.Level.MEDIUM,
        )

    private fun hentPersonMedRelasjonerOgAdressebeskyttelse(
        personIdenter: List<String>,
        tema: Tema,
    ): Map<String, PdlPersonMedRelasjonerOgAdressebeskyttelse> {
        if (personIdenter.isEmpty()) return emptyMap()
        return pdlClientCredentialRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(personIdenter, tema)
    }
}
