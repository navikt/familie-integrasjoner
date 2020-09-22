package no.nav.familie.integrasjoner.arbeidsfordeling

import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.personopplysning.internal.ADRESSEBESKYTTELSEGRADERING
import org.springframework.stereotype.Service

@Service
class ArbeidsfordelingService(
        private val klient: ArbeidsfordelingClient,
        private val pdlRestClient: PdlRestClient) {

    fun finnBehandlendeEnhet(tema: String,
                             geografi: String?,
                             diskresjonskode: String?): List<ArbeidsfordelingClient.Enhet> =
            klient.finnBehandlendeEnhet(tema, geografi, diskresjonskode)

    fun finnBehandlendeEnhetForPerson(personIdent: String, tema: String): List<ArbeidsfordelingClient.Enhet> {

        val behandlendeEnhetData = pdlRestClient.hentBehandlendeEnhetData(personIdent, tema)
        val geografiskTilknytning = behandlendeEnhetData.geografiskTilknytning.gtKommune
        val diskresjonskode = when (behandlendeEnhetData.adressebeskyttelse.firstOrNull()?.gradering) {
            ADRESSEBESKYTTELSEGRADERING.FORTROLIG -> Diskresjonskode.KODE7.kode
            ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG -> Diskresjonskode.KODE6.kode
            ADRESSEBESKYTTELSEGRADERING.STRENGT_FORTROLIG_UTLAND -> Diskresjonskode.KODE6.kode
            else -> null
        }

        return klient.finnBehandlendeEnhet(tema, geografiskTilknytning, diskresjonskode)
    }


    enum class Diskresjonskode(val kode: String) {
        KODE6("SPSF"),
        KODE7("SPFO")
    }

}
