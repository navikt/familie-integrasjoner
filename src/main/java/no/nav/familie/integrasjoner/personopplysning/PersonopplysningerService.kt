package no.nav.familie.integrasjoner.personopplysning

import no.nav.familie.integrasjoner.client.rest.PdlClientCredentialRestClient
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.client.rest.PersonInfoQuery
import no.nav.familie.integrasjoner.client.soap.PersonSoapClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.ws.DateUtil
import no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent
import no.nav.familie.integrasjoner.personopplysning.domene.PersonhistorikkInfo
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import no.nav.familie.integrasjoner.personopplysning.domene.TpsOversetter
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import no.nav.familie.integrasjoner.personopplysning.internal.Adressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.FORELDERBARNRELASJONROLLE
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonMedRelasjonerOgAdressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedAdresseBeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedRelasjoner
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.personopplysning.FinnPersonidenterResponse
import no.nav.familie.kontrakter.felles.personopplysning.PersonIdentMedHistorikk
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Periode
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope
import java.time.LocalDate
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent as TpsPersonIdent

@Service
@ApplicationScope
class PersonopplysningerService(private val personSoapClient: PersonSoapClient,
                                private val oversetter: TpsOversetter,
                                private val pdlRestClient: PdlRestClient,
                                private val pdlClientCredentialRestClient: PdlClientCredentialRestClient) {

    @Deprecated("Tps er markert for utfasing. PDL er master.")
    fun hentHistorikkFor(personIdent: String, fom: LocalDate, tom: LocalDate): PersonhistorikkInfo {
        val request = HentPersonhistorikkRequest()
        request.aktoer = TpsPersonIdent().withIdent(NorskIdent().withIdent(personIdent))
        request.periode = Periode().withFom(DateUtil.convertToXMLGregorianCalendar(fom))
                .withTom(DateUtil.convertToXMLGregorianCalendar(tom))
        val response = personSoapClient.hentPersonhistorikkResponse(request)
        return oversetter.tilPersonhistorikkInfo(PersonIdent(personIdent), response)
    }

    @Deprecated("Tps er markert for utfasing. PDL er master.")
    fun hentPersoninfoFor(personIdent: String?): Personinfo {
        val request: HentPersonRequest = HentPersonRequest()
                .withAktoer(TpsPersonIdent().withIdent(NorskIdent().withIdent(personIdent)))
                .withInformasjonsbehov(listOf(Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.ADRESSE))
        val response = personSoapClient.hentPersonResponse(request)
        return oversetter.tilPersoninfo(PersonIdent(personIdent), response)
    }

    fun hentPersoninfo(personIdent: String,
                       tema: Tema,
                       personInfoQuery: PersonInfoQuery): Person {
        return pdlRestClient.hentPerson(personIdent, tema, personInfoQuery)
    }

    fun hentIdenter(personIdent: String, tema: Tema, medHistorikk: Boolean): FinnPersonidenterResponse {
        val response = pdlRestClient.hentIdenter(personIdent, "FOLKEREGISTERIDENT", tema, medHistorikk)
        return FinnPersonidenterResponse(response.map { PersonIdentMedHistorikk(it.ident, it.historisk) })
    }

    @Cacheable(cacheNames = ["PERSON_MED_RELASJONER"], key = "#personIdent + #tema", condition = "#personIdent != null")
    fun hentPersonMedRelasjoner(personIdent: String, tema: Tema): PersonMedRelasjoner {
        val hovedperson = hentPersonMedRelasjonerOgAdressebeskyttelse(listOf(personIdent), tema).getOrThrow(personIdent)
        val barnIdenter = hovedperson.forelderBarnRelasjon
                .filter { it.relatertPersonsRolle == FORELDERBARNRELASJONROLLE.BARN }
                .map { it.relatertPersonsIdent }
        val sivilstandIdenter = hovedperson.sivilstand.mapNotNull { it.relatertVedSivilstand }.distinct()
        val fullmaktIdenter = hovedperson.fullmakt.map { it.motpartsPersonident }.distinct()

        val tilknyttedeIdenter = (barnIdenter + sivilstandIdenter + fullmaktIdenter).distinct()
        val tilknyttedeIdenterData = hentPersonMedRelasjonerOgAdressebeskyttelse(tilknyttedeIdenter, tema)

        val barnOpplysninger = tilknyttedeIdenterData.filter { (ident, _) -> barnIdenter.contains(ident) }
        val barnsForeldrer = hentBarnsForeldrer(barnOpplysninger, personIdent, tema)

        return PersonMedRelasjoner(personIdent = personIdent,
                                   adressebeskyttelse = hovedperson.gradering(),
                                   sivilstand = mapPersonMedRelasjoner(sivilstandIdenter, tilknyttedeIdenterData),
                                   fullmakt = mapPersonMedRelasjoner(fullmaktIdenter, tilknyttedeIdenterData),
                                   barn = mapPersonMedRelasjoner(barnIdenter, tilknyttedeIdenterData),
                                   barnsForeldrer = barnsForeldrer)
    }

    fun hentAdressebeskyttelse(personIdent: String, tema: Tema): Adressebeskyttelse {
        return pdlRestClient.hentAdressebeskyttelse(personIdent, tema).person.adressebeskyttelse.firstOrNull()
               ?: Adressebeskyttelse(gradering = ADRESSEBESKYTTELSEGRADERING.UGRADERT)
    }

    private fun hentBarnsForeldrer(barnOpplysninger: Map<String, PdlPersonMedRelasjonerOgAdressebeskyttelse>,
                                   personIdent: String,
                                   tema: Tema): List<PersonMedAdresseBeskyttelse> {
        val barnsForeldrerIdenter = barnOpplysninger.flatMap { (_, personMedRelasjoner) ->
            personMedRelasjoner.forelderBarnRelasjon.filter { it.relatertPersonsRolle != FORELDERBARNRELASJONROLLE.BARN }
        }.filter { it.relatertPersonsIdent != personIdent }.map { it.relatertPersonsIdent }.distinct()
        val barnsForeldrerOpplysninger = hentPersonMedRelasjonerOgAdressebeskyttelse(barnsForeldrerIdenter, tema)

        return mapPersonMedRelasjoner(barnsForeldrerIdenter, barnsForeldrerOpplysninger)
    }

    private fun mapPersonMedRelasjoner(sivilstandIdenter: List<String>,
                                       tilknyttedeIdenterData: Map<String, PdlPersonMedRelasjonerOgAdressebeskyttelse>) =
            sivilstandIdenter.map { PersonMedAdresseBeskyttelse(it, tilknyttedeIdenterData.getOrThrow(it).gradering()) }

    private fun PdlPersonMedRelasjonerOgAdressebeskyttelse.gradering() = this.adressebeskyttelse.firstOrNull()?.gradering

    private fun Map<String, PdlPersonMedRelasjonerOgAdressebeskyttelse>.getOrThrow(ident: String) =
            this[ident] ?: throw OppslagException("Finner ikke $ident i response fra pdl",
                                                  "pdl",
                                                  OppslagException.Level.MEDIUM)

    private fun hentPersonMedRelasjonerOgAdressebeskyttelse(personIdenter: List<String>,
                                                            tema: Tema): Map<String, PdlPersonMedRelasjonerOgAdressebeskyttelse> {
        if (personIdenter.isEmpty()) return emptyMap()
        return pdlClientCredentialRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(personIdenter, tema)
    }

    companion object {

        const val PERSON = "PERSON"
    }
}

