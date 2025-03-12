package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype

sealed interface Dokumentmetadata {
    val journalpostType: JournalpostType
    val fagsakSystem: Fagsystem?
    val tema: Tema
    val behandlingstema: Behandlingstema?
    val kanal: String?
    val dokumenttype: Dokumenttype
    val tittel: String?
    val brevkode: String? // NB: Maks lengde som er støttet i joark er 50 tegn
    val dokumentKategori: Dokumentkategori
}

enum class Dokumentkategori(
    private val beskrivelse: String,
) {
    B("Brev"),
    VB("Vedtaksbrev"),
    IB("Infobrev"),
    ES("Elektronisk skjema"),
    TS("Tolkbart skjema"),
    IS("Ikke tolkbart skjema"),
    KS("Konverterte data fra system"),
    KD("Konvertert fra elektronisk arkiv"),
    SED("SED"),
    PUBL_BLANKETT_EOS("Pb EØS"),
    ELEKTRONISK_DIALOG("Elektronisk dialog"),
    REFERAT("Referat"),
    FORVALTNINGSNOTAT("Forvaltningsnotat"), // DENNE BLIR SYNLIG TIL SLUTTBRUKER!
    SOK("Søknad"),
    KA("Klage eller anke"),
}

fun Dokumenttype.tilMetadata(): Dokumentmetadata =
    when (this) {
        Dokumenttype.BARNETILSYN_BLANKETT_SAKSBEHANDLING -> BarnetilsynBlankettSaksbehandlingMetadata
        Dokumenttype.BARNETILSYNSTØNAD_ETTERSENDING -> BarnetilsynEttersendingMetadata
        Dokumenttype.BARNETILSYN_FRITTSTÅENDE_BREV -> BarnetilsynFrittståendeBrevMetadata
        Dokumenttype.BARNETILSYNSTØNAD_SØKNAD -> BarnetilsynSøknadMetadata
        Dokumenttype.BARNETILSYN_TILBAKEKREVING_BREV -> BarnetilsynTilbakebetalingBrevMetadata
        Dokumenttype.BARNETILSYN_TILBAKEKREVING_VEDTAK -> BarnetilsynTilbakebetalingVedtakMetadata
        Dokumenttype.BARNETILSYNSTØNAD_VEDLEGG -> BarnetilsynVedleggMetadata
        Dokumenttype.BARNETRYGD_FORLENGET_SVARTIDSBREV_INSTITUSJON -> BarnetrygdForlengetSvartidsbrevInstitusjonMetadata
        Dokumenttype.BARNETRYGD_FORLENGET_SVARTIDSBREV -> BarnetrygdForlengetSvartidsbrevMetadata
        Dokumenttype.BARNETRYGD_HENLEGGE_TRUKKET_SØKNAD -> BarnetrygdHenleggelseMetadata
        Dokumenttype.BARNETRYGD_HENLEGGE_TRUKKET_SØKNAD_INSTITUSJON -> BarnetrygdHenleggelseInstitusjonMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_DELT_BOSTED -> BarnetrygdInformasjonsbrevDeltBostedMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_FØDSEL_GENERELL -> BarnetrygdInformasjonsbrevFødselGenerell
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_FØDSEL_MINDREÅRIG -> BarnetrygdInformasjonsbrevFødselMindreårigMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_FØDSEL_UMYNDIG -> BarnetrygdInformasjonsbrevFødselUmyndigMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_FØDSEL_VERGEMÅL -> BarnetrygdInformasjonsbrevFødselVergemålMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_KAN_SØKE_EØS -> BarnetrygdInformasjonsbrevKanSøkeEøsMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_KAN_SØKE -> BarnetrygdInformasjonsbrevKanSøkeMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_OM_VALUTAJUSTERING -> BarnetrygdInformasjonsbrevOmValutajusteringMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_TIL_FORELDER_MED_SELVSTENDIG_RETT_VI_HAR_FÅTT_F016_KAN_SØKE_OM_BARNETRYGD -> BarnetrygdInformasjonsbrevTilForelderMedSelvstendigRettViHarFåttF016KanSøkeOmBarnetrygdMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_TIL_FORELDER_OMFATTET_NORSK_LOVGIVNING_HAR_FÅTT_EN_SØKNAD_FRA_ANNEN_FORELDER -> BarnetrygdInformasjonsbrevTilForelderOmfattetNorskLovgivningHarFåttEnSøknadFraAnnenForelderMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_TIL_FORELDER_OMFATTET_NORSK_LOVGIVNING_HAR_GJORT_VEDTAK_TIL_ANNEN_FORELDER -> BarnetrygdInformasjonsbrevTilForelderOmfattetNorskLovgivningHarGjortVedtakTilAnnenForelderMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_TIL_FORELDER_OMFATTET_NORSK_LOVGIVNING_VARSEL_OM_ÅRLIG_KONTROLL -> BarnetrygdInformasjonsbrevTilForelderOmfattetNorskLovgivningVarselOmÅrligKontrollMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_TIL_FORELDER_OMFATTET_NORSK_LOVGIVNING_HENTER_IKKE_REGISTEROPPLYSNINGER -> BarnetrygdInformasjonsbrevTilForelderOmfattetNorskLovgivningHenterIkkeRegisteropplysningerMetadata
        Dokumenttype.BARNETRYGD_INFORMASJONSBREV_KAN_HA_RETT_TIL_PENGESTØTTE_FRA_NAV -> BarnetrygdInformasjonsbrevKanHaRettTilPengestøtteFraNavMetadata
        Dokumenttype.BARNETRYGD_INNHENTE_OPPLYSNINGER_ETTER_SØKNAD_I_SED -> BarnetrygdInnhenteOpplysningerEtterSøknadISedMetadata
        Dokumenttype.BARNETRYGD_INNHENTE_OPPLYSNINGER_INSTITUSJON -> BarnetrygdInnhenteOpplysningerInstitusjonMetadata
        Dokumenttype.BARNETRYGD_INNHENTE_OPPLYSNINGER -> BarnetrygdInnhenteOpplysningerMetadata
        Dokumenttype.BARNETRYGD_INNHENTE_OPPLYSNINGER_OG_INFORMASJON_OM_AT_ANNEN_FORELDER_MED_SELVSTENDIG_RETT_HAR_SØKT -> BarnetrygdInnhenteOpplysningerOgInformasjonOmAtAnnenForelderMedSelvstendigRettHarSøktMetadata
        Dokumenttype.BARNETRYGD_OPPHØR -> BarnetrygdOpphørMetadata
        Dokumenttype.BARNETRYGD_SVARTIDSBREV_INSTITUSJON -> BarnetrygdSvartidsbrevInstitusjonMetadata
        Dokumenttype.BARNETRYGD_SVARTIDSBREV -> BarnetrygdSvartidsbrevMetadata
        Dokumenttype.BARNETRYGD_EØS -> BarnetrygdSøknadEØSMetadata
        Dokumenttype.BARNETRYGD_ORDINÆR -> BarnetrygdSøknadOrdinærMetadata
        Dokumenttype.BARNETRYGD_UTVIDET -> BarnetrygdSøknadUtvidetMetadata
        Dokumenttype.BARNETRYGD_TILBAKEKREVING_BREV -> BarnetrygdTilbakebetalingBrevMetadata
        Dokumenttype.BARNETRYGD_TILBAKEKREVING_VEDTAK -> BarnetrygdTilbakebetalingVedtakMetadata
        Dokumenttype.BARNETRYGD_UTBETALING_ETTER_KA_VEDTAK -> BarnetrygdUtbetalingEtterKAVedtakMetadata
        Dokumenttype.BARNETRYGD_VARSEL_OM_REVURDERING_DELT_BOSTED_PARAGRAF_14 -> BarnetrygdVarselOmRevurderingDeltBostedParagraf14Metadata
        Dokumenttype.BARNETRYGD_VARSEL_OM_REVURDERING_FRA_NASJONAL_TIL_EØS -> BarnetrygdVarselOmRevurderingFraNasjonalTilEøsMetadata
        Dokumenttype.BARNETRYGD_VARSEL_OM_REVURDERING_INSTITUSJON -> BarnetrygdVarselOmRevurderingInstitusjonMetadata
        Dokumenttype.BARNETRYGD_VARSEL_OM_REVURDERING -> BarnetrygdVarselOmRevurderingMetadata
        Dokumenttype.BARNETRYGD_VARSEL_OM_REVURDERING_SAMBOER -> BarnetrygdVarselOmRevurderingSamboerMetadata
        Dokumenttype.BARNETRYGD_VARSEL_OM_VEDTAK_ETTER_SØKNAD_I_SED -> BarnetrygdVarselOmVedtakEtterSøknadMetadata
        Dokumenttype.BARNETRYGD_VARSEL_OM_ÅRLIG_REVURDERING_EØS_MED_INNHENTING_AV_OPPLYSNINGER -> BarnetrygdVarselOmÅrligRevurderingEøsMedInnhentingAvOpplysningerMetadata
        Dokumenttype.BARNETRYGD_VARSEL_OM_ÅRLIG_REVURDERING_EØS -> BarnetrygdVarselOmÅrligRevurderingEøsMetadata
        Dokumenttype.BARNETRYGD_VARSEL_ANNEN_FORELDER_MED_SELVSTENDIG_RETT_SØKT -> BarnetrygdVarselAnnenForelderMedSelvstendigRettSøkt
        Dokumenttype.BARNETRYGD_VEDLEGG -> BarnetrygdVedleggMetadata
        Dokumenttype.BARNETRYGD_VEDTAK_AVSLAG -> BarnetrygdVedtakAvslagMetadata
        Dokumenttype.BARNETRYGD_VEDTAK_INNVILGELSE -> BarnetrygdVedtakInnvilgetMetadata
        Dokumenttype.BARNETRYGD_VEDTAK -> BarnetrygdVedtakMetadata
        Dokumenttype.KLAGE_BLANKETT_SAKSBEHANDLING_BARNETILSYN -> KlageBlankettSaksbehandlingBarnetilsyn
        Dokumenttype.KLAGE_BLANKETT_SAKSBEHANDLING_BARNETRYGD -> KlageBlankettSaksbehandlingBarnetrygd
        Dokumenttype.KLAGE_BLANKETT_SAKSBEHANDLING_KONTANTSTØTTE -> KlageBlankettSaksbehandlingKontantstøtte
        Dokumenttype.KLAGE_BLANKETT_SAKSBEHANDLING_OVERGANGSSTØNAD -> KlageBlankettSaksbehandlingOvergangsstønad
        Dokumenttype.KLAGE_BLANKETT_SAKSBEHANDLING_SKOLEPENGER -> KlageBlankettSaksbehandlingSkolepenger
        Dokumenttype.KLAGE_VEDTAKSBREV_BARNETILSYN -> KlageVedtakBarnetilsyn
        Dokumenttype.KLAGE_VEDTAKSBREV_BARNETRYGD -> KlageVedtakBarnetrygd
        Dokumenttype.KLAGE_VEDTAKSBREV_KONTANTSTØTTE -> KlageVedtakKontantstøtte
        Dokumenttype.KLAGE_VEDTAKSBREV_OVERGANGSSTØNAD -> KlageVedtakOvergangsstønad
        Dokumenttype.KLAGE_VEDTAKSBREV_SKOLEPENGER -> KlageVedtakSkolepenger
        Dokumenttype.KONTANTSTØTTE_SØKNAD -> KontanstøtteSøknadMetadata
        Dokumenttype.KONTANTSTØTTE_SØKNAD_VEDLEGG -> KontanstøtteSøknadVedleggMetadata
        Dokumenttype.KONTANTSTØTTE_FORLENGET_SVARTIDSBREV -> KontantstøtteForlengetSvartidsbrevMetadata
        Dokumenttype.KONTANTSTØTTE_HENLEGGE_TRUKKET_SØKNAD -> KontantstøtteHenleggeTrukketSøknadMetadata
        Dokumenttype.KONTANTSTØTTE_INFORMASJONSBREV_DELT_BOSTED -> KontantstøtteInformasjonsbrevDeltBostedMetadata
        Dokumenttype.KONTANTSTØTTE_INFORMASJONSBREV_KAN_SØKE_EØS -> KontantstøtteInformasjonsbrevKanSøkeEøsMetadata
        Dokumenttype.KONTANTSTØTTE_INFORMASJONSBREV_KAN_SØKE -> KontantstøtteInformasjonsbrevKanSøkeMetadata
        Dokumenttype.KONTANTSTØTTE_INFORMASJONSBREV_TIL_FORELDER_OMFATTET_NORSK_LOVGIVNING_HAR_FÅTT_EN_SØKNAD_FRA_ANNEN_FORELDER -> KontantstøtteInformasjonsbrevTilForelderOmfattetNorskLovgivningHarFåttEnSøknadFraAnnenForelderMetadata
        Dokumenttype.KONTANTSTØTTE_INFORMASJONSBREV_TIL_FORELDER_OMFATTET_NORSK_LOVGIVNING_VARSEL_OM_REVURDERING -> KontantstøtteInformasjonsbrevTilForelderOmfattetNorskLovgivningVarselOmRevurdering
        Dokumenttype.KONTANTSTØTTE_INFORMASJONSBREV_TIL_FORELDER_OMFATTET_NORSK_LOVGIVNING_HENTER_IKKE_REGISTEROPPLYSNINGER -> KontantstøtteInformasjonsbrevTilForelderOmfattetNorskLovgivningHenterIkkeRegisteropplysningerMetadata
        Dokumenttype.KONTANTSTØTTE_INFORMASJONSBREV_KAN_HA_RETT_TIL_PENGESTØTTE_FRA_NAV -> KontantstøtteInformasjonsbrevKanHaRettTilPengestøtteFraNavMetadata
        Dokumenttype.KONTANTSTØTTE_INNHENTE_OPPLYSNINGER_ETTER_SØKNAD_I_SED -> KontantstøtteInnhenteOpplysningerEtterSøknadISedMetadata
        Dokumenttype.KONTANTSTØTTE_INNHENTE_OPPLYSNINGER -> KontantstøtteInnhenteOpplysningerMetadata
        Dokumenttype.KONTANTSTØTTE_INNHENTE_OPPLYSNINGER_OG_INFORMASJON_OM_AT_ANNEN_FORELDER_MED_SELVSTENDIG_RETT_HAR_SØKT -> KontantstøtteInnhenteOpplysningerOgInformasjonOmAtAnnenForelderMedSelvstendigRettHarSøktMetadata
        Dokumenttype.KONTANTSTØTTE_INFORMASJONSBREV_LOVENDRING_JULI_2024 -> KontantstøtteInformasjonsbrevJuli2024
        Dokumenttype.KONTANTSTØTTE_INFORMASJONSBREV_OVERGANGSORDNING_NOVEMBER_2024 -> KontantstøtteInformasjonsbrevOvergangsordningNovember2024Metadata
        Dokumenttype.KONTANTSTØTTE_ENDRING_AV_FRAMTIDIG_OPPHØR -> KontantstøtteEndringAvFramtidigOpphør
        Dokumenttype.KONTANTSTØTTE_VARSEL_ANNEN_FORELDER_MED_SELVSTENDIG_RETT_SØKT -> KontantstøtteVarselAnnenForelderMedSelvstendigRettSøkt
        Dokumenttype.KONTANTSTØTTE_OPPHØR -> KontantstøtteOpphørMetadata
        Dokumenttype.KONTANTSTØTTE_SVARTIDSBREV -> KontantstøtteSvartidsbrevMetadata
        Dokumenttype.KONTANTSTØTTE_EØS -> KontantstøtteSøknadEØSMetadata
        Dokumenttype.KONTANTSTØTTE_TILBAKEKREVING_BREV -> KontantstøtteTilbakebetalingBrevMetadata
        Dokumenttype.KONTANTSTØTTE_TILBAKEKREVING_VEDTAK -> KontantstøtteTilbakebetalingVedtakMetadata
        Dokumenttype.KONTANTSTØTTE_UTBETALING_ETTER_KA_VEDTAK -> KontantstøtteUtbetalingEtterKAVedtakMetadata
        Dokumenttype.KONTANTSTØTTE_VARSEL_OM_REVURDERING_FRA_NASJONAL_TIL_EØS -> KontantstøtteVarselOmRevurderingFraNasjonalTilEøsMetadata
        Dokumenttype.KONTANTSTØTTE_VARSEL_OM_REVURDERING -> KontantstøtteVarselOmRevurderingMetadata
        Dokumenttype.KONTANTSTØTTE_VARSEL_OM_VEDTAK_ETTER_SØKNAD_I_SED -> KontantstøtteVarselOmVedtakEtterSøknadISedMetadata
        Dokumenttype.KONTANTSTØTTE_VEDLEGG -> KontantstøtteVedleggMetadata
        Dokumenttype.KONTANTSTØTTE_VEDTAK_AVSLAG -> KontantstøtteVedtakAvslagMetadata
        Dokumenttype.KONTANTSTØTTE_VEDTAK_INNVILGELSE -> KontantstøtteVedtakInnvilgetMetadata
        Dokumenttype.KONTANTSTØTTE_VEDTAK_ENDRET -> KontantstøtteVedtakEndretMetadata
        Dokumenttype.KONTANTSTØTTE_VEDTAK -> KontantstøtteVedtakMetadata
        Dokumenttype.OVERGANGSSTØNAD_BLANKETT -> OvergangsstønadBlankettMetadata
        Dokumenttype.OVERGANGSSTØNAD_BLANKETT_SAKSBEHANDLING -> OvergangsstønadBlankettSaksbehandlingMetadata
        Dokumenttype.OVERGANGSSTØNAD_ETTERSENDING -> OvergangsstønadEttersendingMetadata
        Dokumenttype.OVERGANGSSTØNAD_FRITTSTÅENDE_BREV -> OvergangsstønadFrittståendeBrevMetadata
        Dokumenttype.OVERGANGSSTØNAD_SØKNAD -> OvergangsstønadSøknadMetadata
        Dokumenttype.OVERGANGSSTØNAD_TILBAKEKREVING_BREV -> OvergangsstønadTilbakebetalingBrevMetadata
        Dokumenttype.OVERGANGSSTØNAD_TILBAKEKREVING_VEDTAK -> OvergangsstønadTilbakebetalingVedtakMetadata
        Dokumenttype.OVERGANGSSTØNAD_SØKNAD_VEDLEGG -> OvergangsstønadVedleggMetadata
        Dokumenttype.SKJEMA_ARBEIDSSØKER -> ArbeidsregistreringsskjemaMetadata
        Dokumenttype.SKOLEPENGER_BLANKETT_SAKSBEHANDLING -> SkolepengerBlankettSaksbehandlingMetadata
        Dokumenttype.SKOLEPENGER_ETTERSENDING -> SkolepengerEttersendingMetadata
        Dokumenttype.SKOLEPENGER_FRITTSTÅENDE_BREV -> SkolepengerFrittståendeBrevMetadata
        Dokumenttype.SKOLEPENGER_SØKNAD -> SkolepengerSøknadMetadata
        Dokumenttype.SKOLEPENGER_TILBAKEKREVING_BREV -> SkolepengerTilbakebetalingBrevMetadata
        Dokumenttype.SKOLEPENGER_TILBAKEKREVING_VEDTAK -> SkolepengerTilbakebetalingVedtakMetadata
        Dokumenttype.SKOLEPENGER_VEDLEGG -> SkolepengerVedleggMetadata
        Dokumenttype.VEDTAKSBREV_BARNETILSYN -> BarnetilsynVedtaksbrevMetadata
        Dokumenttype.VEDTAKSBREV_OVERGANGSSTØNAD -> OvergangsstønadVedtaksbrevMetadata
        Dokumenttype.VEDTAKSBREV_SKOLEPENGER -> SkolepengerVedtaksbrevMetadata
        Dokumenttype.BEREGNET_SAMVÆR_NOTAT -> BeregnetSamværNotatMetadata
    }
