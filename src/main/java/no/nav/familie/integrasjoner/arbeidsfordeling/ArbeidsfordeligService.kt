package no.nav.familie.integrasjoner.arbeidsfordeling

import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService
import org.springframework.stereotype.Service

@Service
class ArbeidsfordeligService(
        private val klient: ArbeidsfordelingClient,
        private val personopplysningerService: PersonopplysningerService) {

    fun finnBehandlendeEnhet(tema: String,
                             geografi: String?,
                             diskresjonskode: String?): List<ArbeidsfordelingClient.Enhet> = klient.finnBehandlendeEnhet(tema,
                                                                                                                         geografi,
                                                                                                                         diskresjonskode)

    fun finnBehandlendeEnhetForPerson(personIdent: String, tema: String): List<ArbeidsfordelingClient.Enhet> {
        val personinfo = personopplysningerService.hentPersoninfo(personIdent);
        if (personinfo == null) {
            throw OppslagException("Kan ikke finne personinfo",
                                   "arbeidsfordelingservice.finnBehandlendeEnhetForPerson",
                                   OppslagException.Level.MEDIUM)
        }
        return klient.finnBehandlendeEnhet(tema, personinfo.geografiskTilknytning, personinfo.diskresjonskode)
    }

}
