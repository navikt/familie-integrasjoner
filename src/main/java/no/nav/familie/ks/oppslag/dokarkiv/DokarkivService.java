package no.nav.familie.ks.oppslag.dokarkiv;

import no.nav.familie.ks.oppslag.aktør.AktørService;
import no.nav.familie.ks.oppslag.dokarkiv.api.*;
import no.nav.familie.ks.oppslag.dokarkiv.client.DokarkivClient;
import no.nav.familie.ks.oppslag.dokarkiv.client.domene.*;
import no.nav.familie.ks.oppslag.dokarkiv.client.domene.Dokument;
import no.nav.familie.ks.oppslag.dokarkiv.metadata.AbstractDokumentMetadata;
import no.nav.familie.ks.oppslag.dokarkiv.metadata.DokumentMetadata;
import no.nav.familie.ks.oppslag.dokarkiv.metadata.KontanstøtteSøknadMetadata;
import no.nav.familie.ks.oppslag.dokarkiv.metadata.KontanstøtteSøknadVedleggMetadata;
import no.nav.familie.ks.oppslag.felles.MDCOperations;
import no.nav.familie.ks.oppslag.personopplysning.PersonopplysningerService;
import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import no.nav.familie.ks.oppslag.personopplysning.domene.Personinfo;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private final PersonopplysningerService personopplysningerService;
    private final AktørService aktørService;

    @Autowired
    public DokarkivService(DokarkivClient dokarkivClient, PersonopplysningerService personopplysningerService, AktørService aktørService) {
        this.dokarkivClient = dokarkivClient;
        this.personopplysningerService = personopplysningerService;
        this.aktørService = aktørService;
    }

    public ArkiverDokumentResponse lagInngåendeJournalpost(ArkiverDokumentRequest arkiverDokumentRequest) {
        String fnr = arkiverDokumentRequest.getFnr();
        String navn = hentNavnForFnr(fnr);

        var request = mapTilOpprettJournalpostRequest(fnr, navn, arkiverDokumentRequest.getDokumenter());

        Optional<OpprettJournalpostResponse> response = Optional.ofNullable(dokarkivClient.lagJournalpost(request, arkiverDokumentRequest.isForsøkFerdigstill(), fnr));
        return response.map(this::mapTilArkiverDokumentResponse).orElse(null);
    }

    public void ferdistillJournalpost(String journalpost) {
        dokarkivClient.ferdigstillJournalpost(journalpost);
    }

    private String hentNavnForFnr(String fnr) {
        String navn = null;
        ResponseEntity<String> aktørResponse = aktørService.getAktørId(fnr);
        if (aktørResponse.getStatusCode().is2xxSuccessful()) {
            ResponseEntity<Personinfo> personInfoResponse = personopplysningerService.hentPersoninfoFor(new AktørId(aktørResponse.getBody()));
            if (personInfoResponse.getStatusCode().is2xxSuccessful() && personInfoResponse.getBody() != null) {
                navn = personInfoResponse.getBody().getNavn();
            }
        }

        if (navn == null) {
            throw new RuntimeException("Kan ikke hente navn");
        }
        return navn;
    }

    private OpprettJournalpostRequest mapTilOpprettJournalpostRequest(String fnr, String navn, List<no.nav.familie.ks.oppslag.dokarkiv.api.Dokument> dokumenter) {
        AbstractDokumentMetadata metadataHoveddokument = METADATA.get(dokumenter.get(0).getDokumentType().name());
        Assert.notNull(metadataHoveddokument, "Ukjent dokumenttype " +  dokumenter.get(0).getDokumentType());

        List<Dokument> dokumentRequest = dokumenter.stream()
                .map(this::mapTilDokument)
                .collect(Collectors.toList());

        return new OpprettJournalpostRequest.OpprettJournalpostRequestBuilder().builder()
                .medJournalpostType(JournalpostType.INNGAAENDE)
                .medBehandlingstema(metadataHoveddokument.getBehandlingstema())
                .medKanal(metadataHoveddokument.getKanal())
                .medTittel(metadataHoveddokument.getTittel())
                .medTema(metadataHoveddokument.getTema())
                .medJournalfoerendeEnhet(DokumentMetadata.JOURNALFØRENDE_ENHET)
                .medAvsenderMottaker(new AvsenderMottaker(fnr, IdType.FNR, navn))
                .medBruker(new Bruker(IdType.FNR, fnr))
                .medDokumenter(dokumentRequest)
                .medEksternReferanseId(MDCOperations.getCallId())
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
