package no.nav.familie.integrasjoner.dokarkiv;

import no.nav.familie.integrasjoner.dokarkiv.api.Dokument;
import no.nav.familie.integrasjoner.dokarkiv.client.DokarkivClient;
import no.nav.familie.integrasjoner.personopplysning.PersonopplysningerService;
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo;
import no.nav.familie.integrasjoner.aktør.AktørService;
import no.nav.familie.integrasjoner.dokarkiv.api.*;
import no.nav.familie.integrasjoner.dokarkiv.client.domene.*;
import no.nav.familie.integrasjoner.dokarkiv.metadata.AbstractDokumentMetadata;
import no.nav.familie.integrasjoner.dokarkiv.metadata.DokumentMetadata;
import no.nav.familie.integrasjoner.dokarkiv.metadata.KontanstøtteSøknadMetadata;
import no.nav.familie.integrasjoner.dokarkiv.metadata.KontanstøtteSøknadVedleggMetadata;
import no.nav.familie.integrasjoner.felles.MDCOperations;
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
        Personinfo personInfoResponse = personopplysningerService.hentPersoninfoFor(fnr);
        if (personInfoResponse != null) {
            navn = personInfoResponse.getNavn();
        }

        if (navn == null) {
            throw new RuntimeException("Kan ikke hente navn");
        }
        return navn;
    }

    private OpprettJournalpostRequest mapTilOpprettJournalpostRequest(String fnr, String navn, List<Dokument> dokumenter) {
        AbstractDokumentMetadata metadataHoveddokument = METADATA.get(dokumenter.get(0).getDokumentType().name());
        Assert.notNull(metadataHoveddokument, "Ukjent dokumenttype " + dokumenter.get(0).getDokumentType());

        List<no.nav.familie.integrasjoner.dokarkiv.client.domene.Dokument> dokumentRequest = dokumenter.stream()
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

    private no.nav.familie.integrasjoner.dokarkiv.client.domene.Dokument mapTilDokument(Dokument dokument) {
        AbstractDokumentMetadata metadata = METADATA.get(dokument.getDokumentType().name());
        Assert.notNull(metadata, "Ukjent dokumenttype " + dokument.getDokumentType());

        String variantFormat;
        if (dokument.getFilType().equals(FilType.PDFA)) {
            variantFormat = "ARKIV"; //ustrukturert dokumentDto
        } else {
            variantFormat = "ORIGINAL"; //strukturert dokumentDto
        }

        return no.nav.familie.integrasjoner.dokarkiv.client.domene.Dokument.DokumentBuilder.aDokument()
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
