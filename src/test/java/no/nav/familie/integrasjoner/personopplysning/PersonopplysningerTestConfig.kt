package no.nav.familie.integrasjoner.personopplysning

import no.nav.familie.integrasjoner.client.soap.PersonSoapClient
import no.nav.familie.integrasjoner.felles.ws.DateUtil
import no.nav.familie.kontrakter.ks.søknad.testdata.SøknadTestdata
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate
import java.util.*
import javax.xml.datatype.DatatypeConfigurationException
import javax.xml.datatype.DatatypeFactory

@Configuration
class PersonopplysningerTestConfig {

    @Bean
    @Profile("mock-personopplysninger")
    @Primary
    @Throws(
            HentPersonhistorikkSikkerhetsbegrensning::class,
            HentPersonhistorikkPersonIkkeFunnet::class,
            HentPersonSikkerhetsbegrensning::class,
            HentPersonPersonIkkeFunnet::class) fun personConsumerMock(): PersonSoapClient {
        val personConsumer = Mockito.mock(PersonSoapClient::class.java)
        val personRequestCaptor =
                ArgumentCaptor.forClass(
                        HentPersonRequest::class.java)
        val historikkRequestCaptor =
                ArgumentCaptor.forClass(
                        HentPersonhistorikkRequest::class.java)
        Mockito.`when`(personConsumer.hentPersonhistorikkResponse(
                historikkRequestCaptor.capture())).thenAnswer { invocation: InvocationOnMock? ->
            if (historikkRequestCaptor.value == null) {
                return@thenAnswer null
            }
            val personIdent =
                    historikkRequestCaptor.value!!
                            .aktoer as PersonIdent
            if (SøknadTestdata.barnPersonident == personIdent.ident
                            .ident) {
                return@thenAnswer hentPersonhistorikkResponseBarn()
            }
            hentPersonHistorikkResponse(personIdent.ident
                                                .ident == SøknadTestdata.morPersonident)
        }
        Mockito.`when`(personConsumer.hentPersonResponse(
                personRequestCaptor.capture())).thenAnswer { invocation: InvocationOnMock? ->
            if (personRequestCaptor.value == null) {
                return@thenAnswer null
            }
            val personIdent =
                    personRequestCaptor.value!!
                            .aktoer as PersonIdent
            if (SøknadTestdata.morPersonident == personIdent.ident
                            .ident) {
                return@thenAnswer hentPersonResponseForMor()
            }
            if (SøknadTestdata.barnPersonident == personIdent.ident
                            .ident) {
                return@thenAnswer hentPersonResponseForBarn()
            }
            hentPersonResponseForFar()
        }
        Mockito.doNothing().`when`(personConsumer).ping()
        return personConsumer
    }

    companion object {
        private val TOM = LocalDate.now()
        private val FOM = TOM.minusYears(5)
        private val FOM_BARNET = LocalDate.of(2018, 5, 1)
        private val NORGE = Landkoder().withValue("NOR")
        private val NORSK_ADRESSE = Bostedsadresse()
                .withStrukturertAdresse(Gateadresse()
                                                .withGatenavn("Sannergata")
                                                .withHusnummer(2)
                                                .withPoststed(Postnummer().withValue("0560"))
                                                .withLandkode(NORGE))
        private val MOR_PERSON_IDENT =
                PersonIdent()
                        .withIdent(NorskIdent().withIdent(SøknadTestdata.morPersonident))
        private val BARN_PERSON_IDENT =
                PersonIdent()
                        .withIdent(NorskIdent().withIdent(SøknadTestdata.barnPersonident))
        private val FAR_PERSON_IDENT =
                PersonIdent()
                        .withIdent(NorskIdent().withIdent(SøknadTestdata.farPersonident))

        private fun hentPersonHistorikkResponse(erMor: Boolean): HentPersonhistorikkResponse {
            val response =
                    HentPersonhistorikkResponse()
            response.aktoer = if (erMor) MOR_PERSON_IDENT else FAR_PERSON_IDENT
            response
                    .withStatsborgerskapListe(hentStatsborgerskap(false))
                    .withPersonstatusListe(hentPersonstatus(false))
                    .withMidlertidigAdressePeriodeListe(hentMidlertidigAdresse())
                    .withBostedsadressePeriodeListe(hentBostedsadresse(false))
            return response
        }

        private fun hentPersonhistorikkResponseBarn(): HentPersonhistorikkResponse {
            val response =
                    HentPersonhistorikkResponse()
            response.aktoer = BARN_PERSON_IDENT
            response
                    .withStatsborgerskapListe(hentStatsborgerskap(true))
                    .withBostedsadressePeriodeListe(hentBostedsadresse(true))
                    .withPersonstatusListe(hentPersonstatus(true))
            return response
        }

        private fun hentPersonResponseForMor(): HentPersonResponse {
            val response =
                    HentPersonResponse()
            return response.withPerson(hentPersoninfoMor())
        }

        private fun hentPersonResponseForBarn(): HentPersonResponse {
            val response =
                    HentPersonResponse()
            return response.withPerson(hentPersoninfoBarn())
        }

        private fun hentPersonResponseForFar(): HentPersonResponse {
            val response =
                    HentPersonResponse()
            return response.withPerson(hentPersoninfoFar())
        }

        private fun hentPersoninfoMor(): Person {
            val mor =
                    hentStandardPersoninfo()
            mor
                    .withKjoenn(Kjoenn().withKjoenn(Kjoennstyper().withValue("K")))
                    .withSivilstand(Sivilstand().withSivilstand(Sivilstander().withValue("GIFT")))
                    .withPersonnavn(Personnavn().withSammensattNavn("TEST TESTESEN"))
                    .withHarFraRolleI(hentFamilierelasjonerMor())
                    .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                    .withAktoer(MOR_PERSON_IDENT)
            return mor
        }

        private fun hentPersoninfoFar(): Person {
            val far =
                    hentStandardPersoninfo()
            far
                    .withKjoenn(Kjoenn().withKjoenn(Kjoennstyper().withValue("M")))
                    .withSivilstand(Sivilstand().withSivilstand(Sivilstander().withValue("GIFT")))
                    .withPersonnavn(Personnavn().withSammensattNavn("EKTEMANN TESTESEN"))
                    .withHarFraRolleI(hentFamilierelasjonerFar())
                    .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                    .withAktoer(FAR_PERSON_IDENT)
            return far
        }

        private fun hentPersoninfoBarn(): Person {
            val barn =
                    hentStandardPersoninfo()
            barn.withKjoenn(Kjoenn().withKjoenn(Kjoennstyper().withValue("K")))
                    .withSivilstand(Sivilstand().withSivilstand(Sivilstander().withValue("UGIF")))
                    .withPersonnavn(Personnavn().withSammensattNavn("BARN TESTESEN"))
                    .withHarFraRolleI(hentFamilierelasjonerBarn())
                    .withFoedselsdato(hentFoedselsdato("2018-05-01"))
                    .withAktoer(BARN_PERSON_IDENT)
            return barn
        }

        private fun hentStandardPersoninfo(): Bruker {
            val person =
                    Bruker()
            person.withPersonstatus(Personstatus().withPersonstatus(Personstatuser().withValue("BOSA")))
                    .withGeografiskTilknytning(Bydel().withGeografiskTilknytning("0315"))
                    .withGjeldendePostadressetype(Postadressetyper().withValue("BOSTEDSADRESSE"))
                    .withStatsborgerskap(Statsborgerskap().withLand(NORGE))
                    .withBostedsadresse(NORSK_ADRESSE)
            return person
        }

        private fun hentFamilierelasjonerMor(): Collection<Familierelasjon> {
            val giftMed =
                    Familierelasjon()
            giftMed
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("EKTE"))
                    .withTilPerson(Person()
                                           .withAktoer(PersonIdent()
                                                               .withIdent(NorskIdent()
                                                                                  .withIdent(SøknadTestdata.farPersonident)))
                                           .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("EKTEMANN TESTESEN")))
            val barnet =
                    Familierelasjon()
            barnet
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("BARN"))
                    .withTilPerson(Person()
                                           .withAktoer(BARN_PERSON_IDENT)
                                           .withFoedselsdato(hentFoedselsdato("2018-05-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("BARN TESTESEN")))
            return Arrays.asList(giftMed, barnet)
        }

        private fun hentFamilierelasjonerBarn(): Collection<Familierelasjon> {
            val far =
                    Familierelasjon()
            far
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("FARA"))
                    .withTilPerson(Person()
                                           .withAktoer(PersonIdent()
                                                               .withIdent(NorskIdent()
                                                                                  .withIdent(SøknadTestdata.farPersonident)))
                                           .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("EKTEMANN TESTESEN")))
            val mor =
                    Familierelasjon()
            mor
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("MORA"))
                    .withTilPerson(Person()
                                           .withAktoer(MOR_PERSON_IDENT)
                                           .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("TEST TESTESEN")))
            return Arrays.asList(far, mor)
        }

        private fun hentFamilierelasjonerFar(): Collection<Familierelasjon> {
            val giftMed =
                    Familierelasjon()
            giftMed
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("EKTE"))
                    .withTilPerson(Person()
                                           .withAktoer(MOR_PERSON_IDENT)
                                           .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("TEST TESTESEN")))
            val barnet =
                    Familierelasjon()
            barnet
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("BARN"))
                    .withTilPerson(Person()
                                           .withAktoer(BARN_PERSON_IDENT)
                                           .withFoedselsdato(hentFoedselsdato("2018-05-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("BARN TESTESEN")))
            return Arrays.asList(giftMed, barnet)
        }

        private fun hentFoedselsdato(dato: String): Foedselsdato {
            return try {
                Foedselsdato().withFoedselsdato(DatatypeFactory.newInstance()
                                                        .newXMLGregorianCalendar(dato))
            } catch (e: DatatypeConfigurationException) {
                throw IllegalStateException(e)
            }
        }

        private fun hentPersonstatus(erBarnet: Boolean): Collection<PersonstatusPeriode> {
            val personstatusPeriode =
                    PersonstatusPeriode()
            personstatusPeriode
                    .withPersonstatus(Personstatuser().withValue("BOSA"))
                    .withPeriode(Periode()
                                         .withFom(DateUtil.convertToXMLGregorianCalendar(if (erBarnet) FOM_BARNET else FOM))
                                         .withTom(DateUtil.convertToXMLGregorianCalendar(
                                                 TOM)))
            return listOf(personstatusPeriode)
        }

        private fun hentStatsborgerskap(erBarnet: Boolean): Collection<StatsborgerskapPeriode> {
            val statsborgerskapPeriode =
                    StatsborgerskapPeriode()
            statsborgerskapPeriode
                    .withStatsborgerskap(Statsborgerskap().withLand(NORGE))
                    .withPeriode(Periode()
                                         .withFom(DateUtil.convertToXMLGregorianCalendar(if (erBarnet) FOM_BARNET else FOM))
                                         .withTom(DateUtil.convertToXMLGregorianCalendar(
                                                 TOM)))
            return listOf(statsborgerskapPeriode)
        }

        private fun hentBostedsadresse(erBarnet: Boolean): Collection<BostedsadressePeriode> {
            val bostedsadressePeriode = BostedsadressePeriode()
            bostedsadressePeriode
                    .withBostedsadresse(NORSK_ADRESSE)
                    .withPeriode(Periode()
                                         .withFom(DateUtil.convertToXMLGregorianCalendar(if (erBarnet) FOM_BARNET else FOM))
                                         .withTom(DateUtil.convertToXMLGregorianCalendar(
                                                 TOM)))
            return listOf(bostedsadressePeriode)
        }

        private fun hentMidlertidigAdresse(): Collection<MidlertidigPostadresse> {
            val midlertidigPostadresseUtland = MidlertidigPostadresseUtland()
            midlertidigPostadresseUtland
                    .withUstrukturertAdresse(UstrukturertAdresse()
                                                     .withAdresselinje1("TEST 1")
                                                     .withAdresselinje2("TEST 2")
                                                     .withAdresselinje3("TEST 3")
                                                     .withLandkode(Landkoder().withValue("SWE")))
                    .withPostleveringsPeriode(Gyldighetsperiode()
                                                      .withFom(DateUtil.convertToXMLGregorianCalendar(
                                                              FOM))
                                                      .withTom(DateUtil.convertToXMLGregorianCalendar(
                                                              TOM)))
            return listOf(midlertidigPostadresseUtland)
        }
    }
}