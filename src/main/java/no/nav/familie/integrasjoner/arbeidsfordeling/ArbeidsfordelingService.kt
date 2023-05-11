package no.nav.familie.integrasjoner.arbeidsfordeling

import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.config.getValue
import no.nav.familie.integrasjoner.egenansatt.EgenAnsattService
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningDto
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningType
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import no.nav.familie.integrasjoner.personopplysning.internal.PersonMedAdresseBeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.personIdentMedKode6
import no.nav.familie.integrasjoner.personopplysning.internal.personMedKode7
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
    private val restClient: ArbeidsfordelingRestClient,
    private val pdlRestClient: PdlRestClient,
    private val egenAnsattService: EgenAnsattService,
    private val personopplysningerService: PersonopplysningerService,
    private val cacheManager: CacheManager,
) {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun finnBehandlendeEnhetForPerson(personIdent: String, tema: Tema): List<Enhet> {
        val kriterie = lagArbeidsfordelingKritierieForPerson(personIdent, tema, tema)
        return restClient.finnBehandlendeEnhetMedBesteMatch(kriterie)
    }

    fun finnBehandlendeEnhetForOppfølging(personIdent: String, tema: Tema): List<Enhet> {
        val kriterie = lagArbeidsfordelingKritierieForPerson(personIdent, tema, Tema.OPP)

        return restClient.finnBehandlendeEnhetMedBesteMatch(kriterie)
    }

    private fun lagArbeidsfordelingKritierieForPerson(
        personIdent: String,
        pdlTema: Tema,
        arbeidsfordelingstema: Tema,
    ): ArbeidsfordelingKriterie {
        val personinfo = personopplysningerService.hentPersoninfo(personIdent, pdlTema)
        val geografiskTilknytning = utledGeografiskTilknytningKode(pdlRestClient.hentGeografiskTilknytning(personIdent, pdlTema))
        val diskresjonskode = personinfo.adressebeskyttelseGradering?.diskresjonskode

        return ArbeidsfordelingKriterie(
            tema = arbeidsfordelingstema.name,
            diskresjonskode = diskresjonskode,
            geografiskOmraade = geografiskTilknytning,
            skjermet = egenAnsattService.erEgenAnsatt(personIdent),
        )
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
            val personMedRelasjoner = personopplysningerService.hentPersonMedRelasjoner(personIdent, tema)

            val aktuellePersoner: List<PersonMedAdresseBeskyttelse> =
                listOf(PersonMedAdresseBeskyttelse(personMedRelasjoner.personIdent, personMedRelasjoner.adressebeskyttelse)) +
                    personMedRelasjoner.barn +
                    personMedRelasjoner.barnsForeldrer +
                    personMedRelasjoner.sivilstand
            val egneAnsatte = finnEgneAnsatte(aktuellePersoner)

            val personMedStrengestBehov = utledPersonMedStrengestBehov(
                personIdent = personIdent,
                personerMedAdresseBeskyttelse = aktuellePersoner,
                egneAnsatte = egneAnsatte,
            )
            val geografiskTilknytning = pdlRestClient.hentGeografiskTilknytning(personMedStrengestBehov.personIdent, tema)

            val kriterier = ArbeidsfordelingKriterie(
                tema = tema.name,
                geografiskOmraade = utledGeografiskTilknytningKode(geografiskTilknytning),
                diskresjonskode = personMedStrengestBehov.adressebeskyttelse?.diskresjonskode,
                skjermet = egneAnsatte.contains(personMedStrengestBehov.personIdent),
            )
            restClient.finnBehandlendeEnhetMedBesteMatch(kriterier)
        }
    }

    private fun finnEgneAnsatte(aktuellePersoner: List<PersonMedAdresseBeskyttelse>) =
        egenAnsattService.erEgenAnsatt(aktuellePersoner.map { it.personIdent }.toSet()).filter { it.value }.keys

    private fun utledPersonMedStrengestBehov(
        personIdent: String,
        personerMedAdresseBeskyttelse: List<PersonMedAdresseBeskyttelse>,
        egneAnsatte: Set<String>,
    ): PersonMedAdresseBeskyttelse {
        val personMedStrengestGrad = personerMedAdresseBeskyttelse.personIdentMedKode6()
            ?: egneAnsatte.firstOrNull()
            ?: personerMedAdresseBeskyttelse.personMedKode7()
            ?: personIdent

        return personerMedAdresseBeskyttelse.find { it.personIdent == personMedStrengestGrad }
            ?: error("Noe har gått veldig galt ettersom person strengest grad ikke finnes i listen over aktuelle personer")
    }
}
