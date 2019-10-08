package no.nav.familie.ks.oppslag.dokarkiv;

import no.nav.familie.ks.oppslag.aktør.AktørService;
import no.nav.familie.ks.oppslag.dokarkiv.api.*;
import no.nav.familie.ks.oppslag.dokarkiv.client.DokarkivClient;
import no.nav.familie.ks.oppslag.dokarkiv.client.domene.IdType;
import no.nav.familie.ks.oppslag.dokarkiv.client.domene.JournalpostType;
import no.nav.familie.ks.oppslag.dokarkiv.client.domene.OpprettJournalpostRequest;
import no.nav.familie.ks.oppslag.dokarkiv.client.domene.OpprettJournalpostResponse;
import no.nav.familie.ks.oppslag.dokarkiv.metadata.DokumentMetadata;
import no.nav.familie.ks.oppslag.dokarkiv.metadata.KontanstøtteSøknadMetadata;
import no.nav.familie.ks.oppslag.personopplysning.PersonopplysningerService;
import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import no.nav.familie.ks.oppslag.personopplysning.domene.Personinfo;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;


public class DokarkivServiceTest {
    public static final String NAVN = "Navn Navnesen";
    public static final String FNR = "fnr";
    public static final byte[] PDF_DOK = "dok".getBytes();
    public static final String ARKIV_VARIANTFORMAT = "ARKIV";
    public static final byte[] JSON_DOK = "{}".getBytes();
    public static final String STRUKTURERT_VARIANTFORMAT = "ORIGINAL";
    public static final String JOURNALPOST_ID = "123";
    public static final String FILNAVN = "filnavn";
    public static final String AKTØR_ID_STRING = "aktorid";
    public static final AktørId AKTØR_ID = new AktørId(AKTØR_ID_STRING);

    private DokarkivClient dokarkivClient = mock(DokarkivClient.class);
    private DokarkivService dokarkivService;
    private PersonopplysningerService personopplysningerService = mock(PersonopplysningerService.class);
    private AktørService aktørService = mock(AktørService.class);

    @Before
    public void setUp() {
        dokarkivService = new DokarkivService(dokarkivClient, personopplysningerService, aktørService);
    }

    @Test
    public void skal_mappe_request_til_oppretttJournalpostRequest_av_type_ARKIV_PDFA() {
        final ArgumentCaptor<OpprettJournalpostRequest> captor = ArgumentCaptor.forClass(OpprettJournalpostRequest.class);
        when(aktørService.getAktørId(FNR)).thenReturn(new ResponseEntity<>(AKTØR_ID_STRING, HttpStatus.OK));
        when(personopplysningerService.hentPersoninfoFor(AKTØR_ID)).thenReturn(new ResponseEntity<>(new Personinfo.Builder().medAktørId(AKTØR_ID).medFødselsdato(LocalDate.now()).medNavn(NAVN).build(), HttpStatus.OK));

        ArkiverDokumentRequest dto = new ArkiverDokumentRequest(FNR, NAVN, false, List.of(new Dokument(PDF_DOK, FilType.PDFA, FILNAVN, DokumentType.KONTANTSTØTTE_SØKNAD)));
        dokarkivService.lagInngåendeJournalpost(dto);

        verify(dokarkivClient).lagJournalpost(captor.capture(),eq(false), eq(FNR));
        OpprettJournalpostRequest request = captor.getValue();
        assertOpprettJournalpostRequest(request, "PDFA", PDF_DOK, ARKIV_VARIANTFORMAT);
    }

    @Test
    public void skal_mappe_request_til_oppretttJournalpostRequest_av_type_ORIGINAL_JSON() {
        final ArgumentCaptor<OpprettJournalpostRequest> captor = ArgumentCaptor.forClass(OpprettJournalpostRequest.class);
        when(aktørService.getAktørId(FNR)).thenReturn(new ResponseEntity<>(AKTØR_ID_STRING, HttpStatus.OK));
        when(personopplysningerService.hentPersoninfoFor(AKTØR_ID)).thenReturn(new ResponseEntity<>(new Personinfo.Builder().medAktørId(AKTØR_ID).medFødselsdato(LocalDate.now()).medNavn(NAVN).build(), HttpStatus.OK));

        ArkiverDokumentRequest dto = new ArkiverDokumentRequest(FNR, NAVN,  false, List.of(new Dokument(JSON_DOK, FilType.JSON, FILNAVN, DokumentType.KONTANTSTØTTE_SØKNAD)));
        dokarkivService.lagInngåendeJournalpost(dto);

        verify(dokarkivClient).lagJournalpost(captor.capture(),eq(false), eq(FNR));
        OpprettJournalpostRequest request = captor.getValue();
        assertOpprettJournalpostRequest(request, "JSON", JSON_DOK, STRUKTURERT_VARIANTFORMAT);
    }

    @Test
    public void response_fra_klient_skal_returnere_ArkiverDokumentResponse() {
        OpprettJournalpostResponse responseFraKlient = new OpprettJournalpostResponse();
        responseFraKlient.setJournalpostId(JOURNALPOST_ID);
        responseFraKlient.setJournalpostferdigstilt(true);
        when(dokarkivClient.lagJournalpost(any(OpprettJournalpostRequest.class), anyBoolean(), anyString())).thenReturn(responseFraKlient);
        when(aktørService.getAktørId(FNR)).thenReturn(new ResponseEntity<>(AKTØR_ID_STRING, HttpStatus.OK));
        when(personopplysningerService.hentPersoninfoFor(AKTØR_ID)).thenReturn(new ResponseEntity<>(new Personinfo.Builder().medAktørId(AKTØR_ID).medFødselsdato(LocalDate.now()).medNavn(NAVN).build(), HttpStatus.OK));

        ArkiverDokumentRequest dto = new ArkiverDokumentRequest(FNR, NAVN, false, List.of(new Dokument(JSON_DOK, FilType.JSON, FILNAVN, DokumentType.KONTANTSTØTTE_SØKNAD)));
        ArkiverDokumentResponse arkiverDokumentResponse = dokarkivService.lagInngåendeJournalpost(dto);

        assertThat(arkiverDokumentResponse.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(arkiverDokumentResponse.isFerdigstilt()).isTrue();
    }

    @Test
    public void skal_kaste_exception_hvis_aktørid_not_found() {
        when(aktørService.getAktørId(FNR)).thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        ArkiverDokumentRequest dto = new ArkiverDokumentRequest(FNR, NAVN, false, List.of(new Dokument(PDF_DOK, FilType.PDFA, FILNAVN, DokumentType.KONTANTSTØTTE_SØKNAD)));

        Throwable thrown = catchThrowable(() -> { dokarkivService.lagInngåendeJournalpost(dto); });
        assertThat(thrown).isInstanceOf(RuntimeException.class).withFailMessage("Kan ikke hente navn");
    }



    private void assertOpprettJournalpostRequest(OpprettJournalpostRequest request, String pdfa, byte[] pdfDok, String arkivVariantformat) {
        assertThat(request.getAvsenderMottaker().getId()).isEqualTo(FNR);
        assertThat(request.getAvsenderMottaker().getNavn()).isEqualTo(NAVN);
        assertThat(request.getAvsenderMottaker().getIdType()).isEqualTo(IdType.FNR);
        assertThat(request.getBruker().getId()).isEqualTo(FNR);
        assertThat(request.getBruker().getIdType()).isEqualTo(IdType.FNR);
        assertThat(request.getBehandlingstema()).isEqualTo(KontanstøtteSøknadMetadata.BEHANDLINGSTEMA);
        assertThat(request.getJournalfoerendeEnhet()).isEqualTo(DokumentMetadata.JOURNALFØRENDE_ENHET);
        assertThat(request.getJournalpostType()).isEqualTo(JournalpostType.INNGAAENDE);
        assertThat(request.getKanal()).isEqualTo(KontanstøtteSøknadMetadata.KANAL);
        assertThat(request.getTema()).isEqualTo(KontanstøtteSøknadMetadata.TEMA);
        assertThat(request.getSak()).isNull();
        assertThat(request.getDokumenter().get(0).getTittel()).isEqualTo(KontanstøtteSøknadMetadata.DOKUMENT_TITTEL);
        assertThat(request.getDokumenter().get(0).getBrevkode()).isEqualTo(KontanstøtteSøknadMetadata.BREVKODE);
        assertThat(request.getDokumenter().get(0).getDokumentKategori()).isEqualTo(KontanstøtteSøknadMetadata.DOKUMENT_KATEGORI);
        assertThat(request.getDokumenter().get(0).getDokumentvarianter().get(0).getFiltype()).isEqualTo(pdfa);
        assertThat(request.getDokumenter().get(0).getDokumentvarianter().get(0).getFysiskDokument()).isEqualTo(pdfDok);
        assertThat(request.getDokumenter().get(0).getDokumentvarianter().get(0).getVariantformat()).isEqualTo(arkivVariantformat);
        assertThat(request.getDokumenter().get(0).getDokumentvarianter().get(0).getFilnavn()).isEqualTo(FILNAVN);
    }

}
