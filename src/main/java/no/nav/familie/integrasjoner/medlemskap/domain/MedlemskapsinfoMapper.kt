package no.nav.familie.integrasjoner.medlemskap.domain

import no.nav.familie.integrasjoner.medlemskap.MedlemskapsunntakResponse
import no.nav.familie.kontrakter.felles.medlemskap.Medlemskapsinfo
import no.nav.familie.kontrakter.felles.medlemskap.PeriodeInfo
import no.nav.familie.kontrakter.felles.medlemskap.PeriodeStatus
import no.nav.familie.kontrakter.felles.medlemskap.PeriodeStatusÅrsak

object MedlemskapsinfoMapper {

    fun tilMedlemskapsInfo(responsListe: List<MedlemskapsunntakResponse>): Medlemskapsinfo {
        val gyldigePerioder: List<PeriodeInfo> = responsListe
            .filter { PeriodeStatus.GYLD.name == it.status }
            .map(this::tilPeriodeInfo)
        val avvistePerioder: List<PeriodeInfo> = responsListe
            .filter { PeriodeStatus.AVST.name == it.status }
            .map(this::tilPeriodeInfo)
        val uavklartePerioder: List<PeriodeInfo> = responsListe
            .filter { PeriodeStatus.UAVK.name == it.status }
            .map(this::tilPeriodeInfo)
        return Medlemskapsinfo(
            gyldigePerioder = gyldigePerioder,
            avvistePerioder = avvistePerioder,
            uavklartePerioder = uavklartePerioder,
            personIdent = tilPersonIdent(responsListe),
        )
    }

    private fun tilPeriodeInfo(response: MedlemskapsunntakResponse): PeriodeInfo {
        return PeriodeInfo(
            periodeStatus = PeriodeStatus.valueOf(response.status),
            fom = response.fraOgMed,
            tom = response.tilOgMed,
            dekning = response.dekning,
            grunnlag = response.grunnlag,
            gjelderMedlemskapIFolketrygden = response.medlem,
            periodeStatusÅrsak =
            if (response.statusaarsak == null) null else PeriodeStatusÅrsak.valueOf(response.statusaarsak),
        )
    }

    private fun tilPersonIdent(responseList: List<MedlemskapsunntakResponse>): String {
        val alleIdenter = responseList.map(MedlemskapsunntakResponse::ident).toSet()
        return if (alleIdenter.size == 1) alleIdenter.first() else ""
    }
}
