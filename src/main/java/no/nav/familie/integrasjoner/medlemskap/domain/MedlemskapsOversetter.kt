package no.nav.familie.integrasjoner.medlemskap.domain

import no.nav.familie.integrasjoner.medlemskap.MedlemskapsUnntakResponse

object MedlemskapsOversetter {
    fun tilMedlemskapsInfo(responsListe: List<MedlemskapsUnntakResponse>): MedlemskapsInfo {
        val gyldigePerioder: List<PeriodeInfo> = responsListe
                .filter { PeriodeStatus.GYLD.name == it.status }
                .map(this::tilPeriodeInfo)
        val avvistePerioder: List<PeriodeInfo> = responsListe
                .filter { PeriodeStatus.AVST.name == it.status }
                .map(this::tilPeriodeInfo)
        val uavklartePerioder: List<PeriodeInfo> = responsListe
                .filter { PeriodeStatus.UAVK.name == it.status }
                .map(this::tilPeriodeInfo)
        return MedlemskapsInfo(gyldigePerioder = gyldigePerioder,
                               avvistePerioder = avvistePerioder,
                               uavklartePerioder = uavklartePerioder,
                               personIdent = tilPersonIdent(responsListe))
    }

    private fun tilPeriodeInfo(response: MedlemskapsUnntakResponse): PeriodeInfo {
        return PeriodeInfo(periodeStatus = PeriodeStatus.valueOf(response.status),
                           fom = response.fraOgMed,
                           tom = response.tilOgMed,
                           dekning = response.dekning,
                           grunnlag = response.grunnlag,
                           gjelderMedlemskapIFolketrygden = response.medlem,
                           periodeStatusÅrsak =
                           if (response.statusaarsak == null) null else PeriodeStatusÅrsak.valueOf(response.statusaarsak))
    }

    private fun tilPersonIdent(responseList: List<MedlemskapsUnntakResponse>): String {
        val alleIdenter = responseList.map(MedlemskapsUnntakResponse::ident).toSet()
        return if (alleIdenter.size == 1) alleIdenter.first() else ""
    }
}