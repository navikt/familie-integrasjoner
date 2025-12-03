package no.nav.familie.integrasjoner.baks.søknad

import no.nav.familie.kontrakter.ba.søknad.v4.Søknadstype
import no.nav.familie.kontrakter.ba.søknad.v9.BarnetrygdSøknad
import no.nav.familie.kontrakter.felles.søknad.Søknadsfelt
import no.nav.familie.kontrakter.ks.søknad.v1.RegistrertBostedType
import no.nav.familie.kontrakter.ks.søknad.v1.SIVILSTANDTYPE
import no.nav.familie.kontrakter.ks.søknad.v1.SøknadAdresse
import no.nav.familie.kontrakter.ks.søknad.v1.TekstPåSpråkMap
import no.nav.familie.kontrakter.ks.søknad.v6.KontantstøtteSøknad
import no.nav.familie.kontrakter.ba.søknad.v8.Barn as BarnetrygdBarn
import no.nav.familie.kontrakter.ba.søknad.v8.Søker as BarnetrygdSøker
import no.nav.familie.kontrakter.ks.søknad.v6.Barn as KontantstøtteBarn
import no.nav.familie.kontrakter.ks.søknad.v4.Søker as KontantstøtteSøker

fun lagKontantstøtteSøknad(
    søkerFnr: String,
    barnFnr: String,
): KontantstøtteSøknad =
    KontantstøtteSøknad(
        kontraktVersjon = 6,
        søker = lagKontantstøtteSøker(søkerFnr),
        barn = listOf(lagKontantstøtteBarn(barnFnr)),
        antallEøsSteg = 0,
        dokumentasjon = emptyList(),
        teksterTilPdf = mapOf("testApiNavn" to TekstPåSpråkMap(mapOf("nb" to "Bokmål", "nn" to "Nynorsk", "en" to "Engelsk"))),
        originalSpråk = "NB",
        finnesPersonMedAdressebeskyttelse = false,
        erNoenAvBarnaFosterbarn = lagStringSøknadsfelt("Nei"),
        søktAsylForBarn = lagStringSøknadsfelt("Nei"),
        oppholderBarnSegIInstitusjon = lagStringSøknadsfelt("Nei"),
        barnOppholdtSegTolvMndSammenhengendeINorge = lagStringSøknadsfelt("Ja"),
        erBarnAdoptert = lagStringSøknadsfelt("Nei"),
        mottarKontantstøtteForBarnFraAnnetEøsland = lagStringSøknadsfelt("Nei"),
        harEllerTildeltBarnehageplass = lagStringSøknadsfelt("Nei"),
        erAvdødPartnerForelder = null,
    )

fun <T> lagStringSøknadsfelt(verdi: T): Søknadsfelt<T> =
    Søknadsfelt(
        label = mapOf("no" to ""),
        verdi = mapOf("no" to verdi),
    )

fun lagKontantstøtteSøker(fnr: String): KontantstøtteSøker =
    KontantstøtteSøker(
        harEøsSteg = false,
        ident = lagStringSøknadsfelt(fnr),
        navn = lagStringSøknadsfelt("Navn"),
        statsborgerskap = lagStringSøknadsfelt(listOf("Norge")),
        adresse =
            lagStringSøknadsfelt(
                SøknadAdresse(
                    adressenavn = "Gate",
                    postnummer = null,
                    husbokstav = null,
                    bruksenhetsnummer = null,
                    husnummer = null,
                    poststed = null,
                ),
            ),
        adressebeskyttelse = false,
        sivilstand = lagStringSøknadsfelt(SIVILSTANDTYPE.UOPPGITT),
        borPåRegistrertAdresse = null,
        værtINorgeITolvMåneder = lagStringSøknadsfelt("Ja"),
        planleggerÅBoINorgeTolvMnd = lagStringSøknadsfelt("Ja"),
        yrkesaktivFemÅr = lagStringSøknadsfelt("Ja"),
        erAsylsøker = lagStringSøknadsfelt("Nei"),
        utenlandsoppholdUtenArbeid = lagStringSøknadsfelt("Nei"),
        utenlandsperioder = emptyList(),
        arbeidIUtlandet = lagStringSøknadsfelt("Nei"),
        arbeidsperioderUtland = emptyList(),
        mottarUtenlandspensjon = lagStringSøknadsfelt("Nei"),
        pensjonsperioderUtland = emptyList(),
        arbeidINorge = lagStringSøknadsfelt("Nei"),
        arbeidsperioderNorge = emptyList(),
        pensjonNorge = lagStringSøknadsfelt("Nei"),
        pensjonsperioderNorge = emptyList(),
        andreUtbetalingsperioder = emptyList(),
        idNummer = emptyList(),
        andreUtbetalinger = null,
        adresseISøkeperiode = null,
    )

fun lagKontantstøtteBarn(fnr: String): KontantstøtteBarn =
    KontantstøtteBarn(
        harEøsSteg = false,
        ident = lagStringSøknadsfelt(fnr),
        navn = lagStringSøknadsfelt(""),
        registrertBostedType = lagStringSøknadsfelt(RegistrertBostedType.REGISTRERT_SOKERS_ADRESSE),
        alder = null,
        teksterTilPdf = emptyMap(),
        erFosterbarn = lagStringSøknadsfelt("Nei"),
        oppholderSegIInstitusjon = lagStringSøknadsfelt("Nei"),
        erAdoptert = lagStringSøknadsfelt("Nei"),
        erAsylsøker = lagStringSøknadsfelt("Nei"),
        boddMindreEnn12MndINorge = lagStringSøknadsfelt("Nei"),
        kontantstøtteFraAnnetEøsland = lagStringSøknadsfelt("Nei"),
        harBarnehageplass = lagStringSøknadsfelt("Nei"),
        andreForelderErDød = null,
        utbetaltForeldrepengerEllerEngangsstønad = null,
        mottarEllerMottokEøsKontantstøtte = null,
        pågåendeSøknadFraAnnetEøsLand = null,
        pågåendeSøknadHvilketLand = null,
        planleggerÅBoINorge12Mnd = null,
        eøsKontantstøttePerioder = emptyList(),
        barnehageplassPerioder = emptyList(),
        borFastMedSøker = lagStringSøknadsfelt("Ja"),
        foreldreBorSammen = null,
        søkerDeltKontantstøtte = null,
        andreForelder = null,
        utenlandsperioder = emptyList(),
        søkersSlektsforhold = null,
        søkersSlektsforholdSpesifisering = null,
        borMedAndreForelder = null,
        borMedOmsorgsperson = null,
        adresse = null,
        omsorgsperson = null,
        idNummer = emptyList(),
    )

fun lagBarnetrygdSøknad(
    søkerFnr: String,
    barnFnr: String,
): BarnetrygdSøknad =
    BarnetrygdSøknad(
        kontraktVersjon = 9,
        søker = lagBarnetrygdSøker(søkerFnr),
        barn = listOf(lagBarnetrygdBarn(barnFnr)),
        antallEøsSteg = 0,
        dokumentasjon = emptyList(),
        originalSpråk = "NB",
        finnesPersonMedAdressebeskyttelse = false,
        søknadstype = Søknadstype.ORDINÆR,
        spørsmål = emptyMap(),
        teksterUtenomSpørsmål = emptyMap(),
    )

fun lagBarnetrygdSøker(fnr: String): BarnetrygdSøker =
    BarnetrygdSøker(
        harEøsSteg = false,
        ident = lagStringSøknadsfelt(fnr),
        navn = lagStringSøknadsfelt("Navn"),
        statsborgerskap = lagStringSøknadsfelt(listOf("Norge")),
        adresse =
            lagStringSøknadsfelt(
                no.nav.familie.kontrakter.ba.søknad.v1.SøknadAdresse(
                    adressenavn = "Gate",
                    postnummer = null,
                    husbokstav = null,
                    bruksenhetsnummer = null,
                    husnummer = null,
                    poststed = null,
                ),
            ),
        adressebeskyttelse = false,
        sivilstand = lagStringSøknadsfelt(no.nav.familie.kontrakter.ba.søknad.v1.SIVILSTANDTYPE.UOPPGITT),
        utenlandsperioder = emptyList(),
        arbeidsperioderUtland = emptyList(),
        pensjonsperioderUtland = emptyList(),
        arbeidsperioderNorge = emptyList(),
        pensjonsperioderNorge = emptyList(),
        andreUtbetalingsperioder = emptyList(),
        idNummer = emptyList(),
        spørsmål = emptyMap(),
        nåværendeSamboer = null,
        tidligereSamboere = emptyList(),
    )

fun lagBarnetrygdBarn(fnr: String): BarnetrygdBarn =
    BarnetrygdBarn(
        harEøsSteg = false,
        ident = lagStringSøknadsfelt(fnr),
        navn = lagStringSøknadsfelt(""),
        registrertBostedType = lagStringSøknadsfelt(no.nav.familie.kontrakter.ba.søknad.v5.RegistrertBostedType.REGISTRERT_SOKERS_ADRESSE),
        alder = null,
        andreForelder = null,
        utenlandsperioder = emptyList(),
        omsorgsperson = null,
        idNummer = emptyList(),
        spørsmål = emptyMap(),
        eøsBarnetrygdsperioder = emptyList(),
    )
