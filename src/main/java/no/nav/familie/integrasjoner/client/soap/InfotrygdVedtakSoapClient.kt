package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.http.client.AbstractSoapClient
import no.nav.familie.http.client.Pingable
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.ef.PeriodeOvergangsstønad
import no.nav.familie.kontrakter.felles.ef.PerioderOvergangsstønadRequest
import no.nav.familie.kontrakter.felles.ef.PerioderOvergangsstønadResponse
import no.nav.tjeneste.virksomhet.infotrygdvedtak.v1.binding.InfotrygdVedtakV1
import no.nav.tjeneste.virksomhet.infotrygdvedtak.v1.informasjon.Periode
import no.nav.tjeneste.virksomhet.infotrygdvedtak.v1.informasjon.Stoenadsklasser
import no.nav.tjeneste.virksomhet.infotrygdvedtak.v1.meldinger.FinnInfotrygdVedtakListeRequest
import no.nav.tjeneste.virksomhet.infotrygdvedtak.v1.meldinger.FinnInfotrygdVedtakListeResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

/**
 * Klient for å hente perioder fra en tjeneste som Arena bruker for å hente vedtaksperioder.
 * Skal brukes av ef-sak for å sjekke att periodene er de samme som hentes ut fra replikaen.
 */
@Component
class InfotrygdVedtakSoapClient(private val infotrygdVedtakV1: InfotrygdVedtakV1)
    : AbstractSoapClient("helsesjekk.infotrygdvedtak"), Pingable {

    fun hentVedtaksperioder(request: PerioderOvergangsstønadRequest): PerioderOvergangsstønadResponse {
        val finnInfotrygdVedtakListeRequest = mapRequest(request)
        val response = finnInfotrygdVedtakListeResponse(finnInfotrygdVedtakListeRequest)
        return mapResponse(response)
    }

    private fun finnInfotrygdVedtakListeResponse(request: FinnInfotrygdVedtakListeRequest): FinnInfotrygdVedtakListeResponse {
        try {
            return executeMedMetrics {
                infotrygdVedtakV1.finnInfotrygdVedtaksinformasjon(request)
            }
        } catch (e: Exception) {
            throw OppslagException("Hent vedtaksperioder fra Infotrygd feilet",
                                   "Infotrygd.finnInfotrygdVedtakListeResponse",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.INTERNAL_SERVER_ERROR,
                                   e)
        }
    }

    private fun mapResponse(response: FinnInfotrygdVedtakListeResponse) =
            PerioderOvergangsstønadResponse(response.vedtaksinformasjonListe.map {
                PeriodeOvergangsstønad(it.personident,
                                       it.vedtaksperiode.fom.toLocalDate(),
                                       it.vedtaksperiode.tom.toLocalDate(),
                                       PeriodeOvergangsstønad.Datakilde.INFOTRYGD)
            })

    private fun mapRequest(request: PerioderOvergangsstønadRequest): FinnInfotrygdVedtakListeRequest {
        val finnInfotrygdVedtakListeRequest = FinnInfotrygdVedtakListeRequest()
        val element = Stoenadsklasser()
        element.value = "EF"
        finnInfotrygdVedtakListeRequest.stoenadsklasseListe.add(element)
        val periode = Periode()
        periode.fom = request.fomDato.toXmlDate()
        periode.tom = request.tomDato.toXmlDate()
        if (periode.fom != null || periode.tom != null) {
            finnInfotrygdVedtakListeRequest.periode = periode
        }
        return finnInfotrygdVedtakListeRequest
    }

    override fun ping() {
        infotrygdVedtakV1.ping()
    }

    fun XMLGregorianCalendar.toLocalDate(): LocalDate = LocalDate.of(this.year, this.month, this.day)

    fun LocalDate?.toXmlDate() = this?.let { DatatypeFactory.newInstance().newXMLGregorianCalendar(it.toString()) }
}