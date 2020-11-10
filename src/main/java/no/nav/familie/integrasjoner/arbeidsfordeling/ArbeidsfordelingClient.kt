package no.nav.familie.integrasjoner.arbeidsfordeling

import no.nav.familie.http.client.AbstractSoapClient
import no.nav.familie.http.client.Pingable
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.ArbeidsfordelingKriterier
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Diskresjonskoder
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Geografi
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Tema
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service


@Service
class ArbeidsfordelingClient(private val arbeidsfordelingV1: ArbeidsfordelingV1)
    : AbstractSoapClient("arbeidsfordeling"), Pingable {


    override fun ping() {
        arbeidsfordelingV1.ping()
    }

    fun finnBehandlendeEnhet(gjeldendeTema: String,
                             gjeldendeGeografiskOmråde: String?,
                             gjeldendeDiskresjonskode: String?): List<Enhet> {
        val request = FinnBehandlendeEnhetListeRequest().apply {
            arbeidsfordelingKriterier = ArbeidsfordelingKriterier().apply {
                tema = Tema().apply { value = gjeldendeTema }

                if (gjeldendeGeografiskOmråde != null) {
                    geografiskTilknytning = Geografi().apply {
                        value = gjeldendeGeografiskOmråde
                    }
                }

                if (gjeldendeDiskresjonskode != null) {
                    diskresjonskode = Diskresjonskoder().apply {
                        value = gjeldendeDiskresjonskode
                    }
                }
            }
        }

        return Result.runCatching { arbeidsfordelingV1.finnBehandlendeEnhetListe(request) }
                .map { it.behandlendeEnhetListe.map { enhet -> Enhet(enhet.enhetId, enhet.enhetNavn) } }
                .onFailure {
                    throw OppslagException(
                            "Ugyldig input tema=$gjeldendeTema geografiskOmråde=$gjeldendeGeografiskOmråde melding=${it.message}",
                            "ArbeidsfordelingV1.finnBehandlendeEnhet",
                            OppslagException.Level.MEDIUM,
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            it)
                }
                .getOrThrow()


    }
}
