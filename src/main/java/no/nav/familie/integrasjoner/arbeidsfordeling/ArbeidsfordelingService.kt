package no.nav.familie.integrasjoner.arbeidsfordeling

import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningDto
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningType
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import org.springframework.stereotype.Service
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.navkontor.NavKontorEnhet

@Service
class ArbeidsfordelingService(
        private val klient: ArbeidsfordelingClient,
        private val restClient: ArbeidsfordelingRestClient,
        private val pdlRestClient: PdlRestClient,
        private val personopplysningerService: PersonopplysningerService) {

    fun finnBehandlendeEnhet(tema: String,
                             geografi: String?,
                             diskresjonskode: String?): List<Enhet> =
            klient.finnBehandlendeEnhet(tema, geografi, diskresjonskode)

    fun finnBehandlendeEnhetForPerson(personIdent: String, tema: String): List<Enhet> {
        val personinfo = personopplysningerService.hentPersoninfo(personIdent)
                         ?: throw OppslagException("Kan ikke finne personinfo",
                                                   "arbeidsfordelingservice.finnBehandlendeEnhetForPerson",
                                                   OppslagException.Level.MEDIUM)
        return klient.finnBehandlendeEnhet(tema, personinfo.geografiskTilknytning, personinfo.diskresjonskode)
    }

    fun finnLokaltNavKontor(personIdent: String, tema: String): NavKontorEnhet {
        val geografiskTilknytning = pdlRestClient.hentGeografiskTilknytning(personIdent, tema)

        val geografiskTilknytningKode: String = utledGeografiskTilknytningKode(geografiskTilknytning)

        return restClient.hentEnhet(geografiskTilknytningKode)

    }

    private fun utledGeografiskTilknytningKode(geografiskTilknytning: GeografiskTilknytningDto): String {
        geografiskTilknytning.let {
           return when (it.gtType) {
                GeografiskTilknytningType.BYDEL -> it.gtBydel!!
                GeografiskTilknytningType.KOMMUNE -> it.gtKommune!!
                GeografiskTilknytningType.UTLAND -> it.gtLand!!
                GeografiskTilknytningType.UDEFINERT -> throw IllegalStateException("Kan ikke finne nav-kontor fra geografisk tilknytning=[$it]")
            }
        }
    }


}