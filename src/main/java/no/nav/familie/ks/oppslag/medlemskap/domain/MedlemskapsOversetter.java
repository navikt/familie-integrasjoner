package no.nav.familie.ks.oppslag.medlemskap.domain;

import no.nav.familie.ks.oppslag.medlemskap.MedlemskapsUnntakResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class MedlemskapsOversetter {

    public MedlemskapsInfo tilMedlemskapsInfo(List<MedlemskapsUnntakResponse> responsListe) {
        List<PeriodeInfo> gyldigePerioder = responsListe.stream()
                .filter(r -> PeriodeStatus.GYLD.name().equals(r.getStatus()))
                .map(this::tilPeriodeInfo)
                .collect(toList());

        List<PeriodeInfo> avvistePerioder = responsListe.stream()
                .filter(r -> PeriodeStatus.AVST.name().equals(r.getStatus()))
                .map(this::tilPeriodeInfo)
                .collect(toList());

        List<PeriodeInfo> uavklartePerioder = responsListe.stream()
                .filter(r -> PeriodeStatus.UAVK.name().equals(r.getStatus()))
                .map(this::tilPeriodeInfo)
                .collect(toList());

        return new MedlemskapsInfo.Builder()
                .medGyldigePerioder(gyldigePerioder)
                .medAvvistePerioder(avvistePerioder)
                .medUavklartePerioder(uavklartePerioder)
                .medPersonIdent(tilPersonIdent(responsListe))
                .build();
    }

    private PeriodeInfo tilPeriodeInfo(MedlemskapsUnntakResponse response) {
        PeriodeInfo.Builder builder = new PeriodeInfo.Builder()
                .medPeriodeStatus(PeriodeStatus.valueOf(response.getStatus()))
                .medFom(response.getFraOgMed().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .medTom(response.getTilOgMed().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .medDekning(response.getDekning())
                .medGrunnlag(response.getGrunnlag())
                .medGjelderMedlemskapIFolketrygden(response.isMedlem());
        if (response.getStatusaarsak() != null) {
            builder.medPeriodeStatusÅrsak(PeriodeStatusÅrsak.valueOf(response.getStatusaarsak()));
        }

        return builder.build();
    }

    private String tilPersonIdent(List<MedlemskapsUnntakResponse> responseList) {
        List<String> alleIdenter = responseList.stream()
                .map(MedlemskapsUnntakResponse::getIdent)
                .collect(toList());
        boolean identFinnesOgErLik = !alleIdenter.isEmpty() && alleIdenter.stream().allMatch(alleIdenter.get(0)::equals);

        return identFinnesOgErLik ? alleIdenter.get(0) : "";
    }
}
