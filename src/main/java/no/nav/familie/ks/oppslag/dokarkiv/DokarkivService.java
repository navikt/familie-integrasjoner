package no.nav.familie.ks.oppslag.dokarkiv;

import no.nav.familie.ks.oppslag.dokarkiv.api.*;
import no.nav.familie.ks.oppslag.dokarkiv.client.DokarkivClient;
import no.nav.familie.ks.oppslag.dokarkiv.client.domene.*;
import no.nav.familie.ks.oppslag.dokarkiv.client.domene.Dokument;
import no.nav.familie.ks.oppslag.dokarkiv.metadata.AbstractDokumentMetadata;
import no.nav.familie.ks.oppslag.dokarkiv.metadata.DokumentMetadata;
import no.nav.familie.ks.oppslag.dokarkiv.metadata.KontanstøtteSøknadMetadata;
import no.nav.familie.ks.oppslag.dokarkiv.metadata.KontanstøtteSøknadVedleggMetadata;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DokarkivService {
    private static final Map<String, ? extends AbstractDokumentMetadata> METADATA = Map.of(
            DokumentType.KONTANTSTØTTE_SØKNAD.name(), new KontanstøtteSøknadMetadata(),
            DokumentType.KONTANTSTØTTE_SØKNAD_VEDLEGG.name(), new KontanstøtteSøknadVedleggMetadata()
    );

    private final DokarkivClient dokarkivClient;

    @Autowired
    public DokarkivService(DokarkivClient dokarkivClient) {
        this.dokarkivClient = dokarkivClient;
    }

    public ArkiverDokumentResponse lagInngåendeJournalpost(ArkiverDokumentRequest arkiverDokumentRequest) {
        String fnr = arkiverDokumentRequest.getFnr();
        String navn = arkiverDokumentRequest.getNavn();

        var request = mapTilOpprettJournalpostRequest(fnr, navn, arkiverDokumentRequest.getDokumenter());

        Optional<OpprettJournalpostResponse> response = Optional.ofNullable(dokarkivClient.lagJournalpost(request, arkiverDokumentRequest.isForsøkFerdigstill(), fnr));
        return response.map(this::mapTilArkiverDokumentResponse).orElse(null);
    }

    private OpprettJournalpostRequest mapTilOpprettJournalpostRequest(String fnr, String navn, List<no.nav.familie.ks.oppslag.dokarkiv.api.Dokument> dokumenter) {
        AbstractDokumentMetadata metadataHoveddokument = METADATA.get(dokumenter.get(0).getDokumentType().name());
        Assert.notNull(metadataHoveddokument, "Ukjent dokumenttype " +  dokumenter.get(0).getDokumentType());

        List<Dokument> dokumentRequest = dokumenter.stream()
                .map(s -> mapTilDokument(s))
                .collect(Collectors.toList());

        return new OpprettJournalpostRequest.OpprettJournalpostRequestBuilder().builder()
                .medJournalpostType(JournalpostType.INNGAAENDE)
                .medBehandlingstema(metadataHoveddokument.getBehandlingstema())
                .medKanal(metadataHoveddokument.getKanal())
                .medTema(metadataHoveddokument.getTema())
                .medJournalfoerendeEnhet(DokumentMetadata.JOURNALFØRENDE_ENHET)
                .medAvsenderMottaker(new AvsenderMottaker(fnr, IdType.FNR, navn))
                .medBruker(new Bruker(IdType.FNR, fnr))
                .medDokumenter(dokumentRequest)
                .medEksternReferanseId(MDC.get("correlationId"))
//                .medSak() Når vi tar over fagsak, så må dennne settes til vår. For BRUT001 behandling, så kan ikke denne settes
                .build();

    }

    private Dokument mapTilDokument(no.nav.familie.ks.oppslag.dokarkiv.api.Dokument dokument) {
        AbstractDokumentMetadata metadata = METADATA.get(dokument.getDokumentType().name());
        Assert.notNull(metadata, "Ukjent dokumenttype " +  dokument.getDokumentType());

        String variantFormat;
        if (dokument.getFilType().equals(FilType.PDFA)) {
            variantFormat = "ARKIV"; //ustrukturert dokumentDto
        } else {
            variantFormat = "ORIGINAL"; //strukturert dokumentDto
        }

        return Dokument.DokumentBuilder.aDokument()
                .medBrevkode(metadata.getBrevkode())
                .medDokumentKategori(metadata.getDokumentKategori())
                .medTittel(metadata.getTittel())
                .medDokumentvarianter(List.of(
                        new DokumentVariant(dokument.getFilType().name(),
                                variantFormat,
                                dokument.getDokument(), dokument.getFilnavn())))
                .build();
    }

    private ArkiverDokumentResponse mapTilArkiverDokumentResponse(OpprettJournalpostResponse response) {
        return new ArkiverDokumentResponse(response.getJournalpostId(), response.getJournalpostferdigstilt());
    }
}
