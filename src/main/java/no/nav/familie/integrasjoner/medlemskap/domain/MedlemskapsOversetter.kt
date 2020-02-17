package no.nav.familie.integrasjoner.medlemskap.domain

import no.nav.familie.integrasjoner.medlemskap.MedlemskapsUnntakResponse
import java.time.ZoneId
import java.util.stream.Collectors

object MedlemskapsOversetter {
    fun tilMedlemskapsInfo(responsListe: List<MedlemskapsUnntakResponse?>?): MedlemskapsInfo? {
        val gyldigePerioder: List<PeriodeInfo> = responsListe!!.stream()
                .filter { r: MedlemskapsUnntakResponse? -> PeriodeStatus.GYLD.name == r.getStatus() }
                .map { obj: MedlemskapsUnntakResponse? -> tilPeriodeInfo() }
                .collect(Collectors.toList())
        val avvistePerioder: List<PeriodeInfo> = responsListe.stream()
                .filter { r: MedlemskapsUnntakResponse? -> PeriodeStatus.AVST.name == r.getStatus() }
                .map { obj: MedlemskapsUnntakResponse? -> tilPeriodeInfo() }
                .collect(Collectors.toList())
        val uavklartePerioder: List<PeriodeInfo> = responsListe.stream()
                .filter { r: MedlemskapsUnntakResponse? -> PeriodeStatus.UAVK.name == r.getStatus() }
                .map { obj: MedlemskapsUnntakResponse? -> tilPeriodeInfo() }
                .collect(Collectors.toList())
        return MedlemskapsInfo.Builder()
                .medGyldigePerioder(gyldigePerioder)
                .medAvvistePerioder(avvistePerioder)
                .medUavklartePerioder(uavklartePerioder)
                .medPersonIdent(tilPersonIdent(responsListe))
                .build()
    }

    private fun tilPeriodeInfo(response: MedlemskapsUnntakResponse): PeriodeInfo? {
        val builder =
                PeriodeInfo.Builder()
                        .medPeriodeStatus(PeriodeStatus.valueOf(response.status))
                        .medFom(response.fraOgMed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        .medTom(response.tilOgMed.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        .medDekning(response.dekning)
                        .medGrunnlag(response.grunnlag)
                        .medGjelderMedlemskapIFolketrygden(response.isMedlem!!)
        if (response.statusaarsak != null) {
            builder!!.medPeriodeStatusÅrsak(PeriodeStatusÅrsak.valueOf(response.statusaarsak))
        }
        return builder!!.build()
    }

    private fun tilPersonIdent(responseList: List<MedlemskapsUnntakResponse?>?): String? {
        val alleIdenter = responseList!!.stream()
                .map { obj: MedlemskapsUnntakResponse? -> obj.getIdent() }
                .collect(Collectors.toList())
        val identFinnesOgErLik =
                !alleIdenter.isEmpty() && alleIdenter.stream().allMatch { anObject: String? ->
                    alleIdenter[0]
                            .equals(anObject)
                }
        return if (identFinnesOgErLik) alleIdenter[0] else ""
    }
}