package no.nav.familie.integrasjoner.arbeidsfordeling

import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.config.getValue
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningDto
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningType
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.*
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.annotasjoner.Improvement
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.navkontor.NavKontorEnhet
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class ArbeidsfordelingService(
        private val klient: ArbeidsfordelingClient,
        private val restClient: ArbeidsfordelingRestClient,
        private val pdlRestClient: PdlRestClient,
        private val egenAnsattService: EgenAnsattService,
        private val cacheManager: CacheManager) {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun finnBehandlendeEnhet(tema: Tema,
                             geografi: String?,
                             diskresjonskode: String?): List<Enhet> =
            klient.finnBehandlendeEnhet(tema, geografi, diskresjonskode)

    fun finnBehandlendeEnhetForPerson(personIdent: String, tema: Tema): List<Enhet> {
        val personinfo = pdlRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf(personIdent), tema)[personIdent]
        val geografiskTilknytning = pdlRestClient.hentGeografiskTilknytning(personIdent, tema)
        val geografiskTilknytningKode: String? = utledGeografiskTilknytningKode(geografiskTilknytning)

        return klient.finnBehandlendeEnhet(tema, geografiskTilknytningKode, utledStrengesteDiskresjonskode(personinfo?.adressebeskyttelse).diskresjonskode)
    }

    @Improvement("Må ta høyde for om personIdent har diskresjonskode eller skjerming/er egen ansatt. Nå krasjer den for de med kode 6")
    fun finnLokaltNavKontor(personIdent: String, tema: Tema): NavKontorEnhet? {
        val geografiskTilknytning = pdlRestClient.hentGeografiskTilknytning(personIdent, tema)

        val geografiskTilknytningKode: String? = utledGeografiskTilknytningKode(geografiskTilknytning)
        if (geografiskTilknytningKode == null) {
            secureLogger.info("Fant ikke geografiskTilknytningKode=$geografiskTilknytning for personIdent=$personIdent")
            return null
        }

        return restClient.hentEnhet(geografiskTilknytningKode)
    }

    fun hentNavKontor(enhetsId: String): NavKontorEnhet {
        return restClient.hentNavkontor(enhetsId)
    }

    private fun utledStrengesteDiskresjonskode(adressebeskyttelser: List<Adressebeskyttelse>?): ADRESSEBESKYTTELSEGRADERING {
        if (adressebeskyttelser.isNullOrEmpty()) {
            return ADRESSEBESKYTTELSEGRADERING.UGRADERT
        }
        //bruk høyeste diskresjonskode hvis flere
        if (adressebeskyttelser.any{ it.gradering == ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG} ) {
            return ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG
        } else if (adressebeskyttelser.any{ it.gradering == ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG_UTLAND} ) {
            return ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG_UTLAND
        } else if (adressebeskyttelser.any{ it.gradering == ADRESSEBESKYTTELSEGRADERING.FORTROLIG} ){
            return ADRESSEBESKYTTELSEGRADERING.FORTROLIG
        } else
            return ADRESSEBESKYTTELSEGRADERING.UGRADERT

    }

    private fun utledGeografiskTilknytningKode(geografiskTilknytning: GeografiskTilknytningDto): String? {
        geografiskTilknytning.let {
            return when (it.gtType) {
                GeografiskTilknytningType.BYDEL -> it.gtBydel
                GeografiskTilknytningType.KOMMUNE -> it.gtKommune
                GeografiskTilknytningType.UTLAND -> null
                GeografiskTilknytningType.UDEFINERT -> null
            }
        }
    }


    @Cacheable("enhet_for_person_med_relasjoner")
    fun finnBehandlendeEnhetForPersonMedRelasjoner(personIdent: String, tema: Tema): List<Enhet> {
        return cacheManager.getValue("navEnhet", personIdent) {
            val personMedRelasjoner = pdlRestClient.hentPersonMedRelasjonerOgAdressebeskyttelse(listOf(personIdent), tema)[personIdent]

            val aktuellePersoner: MutableList<PersonMedAdresseBeskyttelse> =
                mutableListOf(PersonMedAdresseBeskyttelse(personIdent, utledStrengesteDiskresjonskode(personMedRelasjoner?.adressebeskyttelse)))

            //Hent diskresjonskode for barn og annen forelder også.
            personMedRelasjoner?.forelderBarnRelasjon?.forEach {
                aktuellePersoner.add(
                    PersonMedAdresseBeskyttelse(it.relatertPersonsIdent,utledStrengesteDiskresjonskode(
                        pdlRestClient.hentAdressebeskyttelse(it.relatertPersonsIdent, tema).person.adressebeskyttelse)))
            }

            val egneAnsatte = finnEgneAnsatte(aktuellePersoner)

            val personMedStrengestBehov = utledPersonMedStrengestBehov(personIdent = personIdent,
                                                                       personerMedAdresseBeskyttelse = aktuellePersoner,
                                                                       egneAnsatte = egneAnsatte)
            val geografiskTilknytning = pdlRestClient.hentGeografiskTilknytning(personMedStrengestBehov.personIdent, tema)

            restClient.finnBehandlendeEnhetMedBesteMatch(
                    gjeldendeTema = tema,
                    gjeldendeGeografiskOmråde = utledGeografiskTilknytningKode(geografiskTilknytning),
                    gjeldendeDiskresjonskode = personMedStrengestBehov.adressebeskyttelse?.diskresjonskode,
                    erEgenAnsatt = egneAnsatte.contains(personMedStrengestBehov.personIdent))
        }

    }

    private fun finnEgneAnsatte(aktuellePersoner: List<PersonMedAdresseBeskyttelse>) =
            egenAnsattService.erEgenAnsatt(aktuellePersoner.map { it.personIdent }.toSet()).filter { it.value }.keys

    private fun utledPersonMedStrengestBehov(personIdent: String,
                                             personerMedAdresseBeskyttelse: List<PersonMedAdresseBeskyttelse>,
                                             egneAnsatte: Set<String>): PersonMedAdresseBeskyttelse {

        val personMedStrengestGrad = personerMedAdresseBeskyttelse.personIdentMedKode6()
                                     ?: egneAnsatte.firstOrNull()
                                     ?: personerMedAdresseBeskyttelse.personMedKode7()
                                     ?: personIdent

        return personerMedAdresseBeskyttelse.find { it.personIdent == personMedStrengestGrad }
               ?: error("Noe har gått veldig galt ettersom person strengest grad ikke finnes i listen over aktuelle personer")

    }


}
