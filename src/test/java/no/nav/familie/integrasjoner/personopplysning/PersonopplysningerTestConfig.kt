package no.nav.familie.integrasjoner.personopplysning

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.soap.PersonSoapClient
import no.nav.familie.integrasjoner.felles.ws.DateUtil
import no.nav.familie.kontrakter.ks.søknad.testdata.SøknadTestdata
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate
import javax.xml.datatype.DatatypeConfigurationException
import javax.xml.datatype.DatatypeFactory

@Configuration
class PersonopplysningerTestConfig {

    @Bean
    @Profile("mock-personopplysninger")
    @Primary
    fun personConsumerMock(): PersonSoapClient {
        val personConsumer = mockk<PersonSoapClient>()
        every { personConsumer.hentPersonhistorikkResponse(any()) } answers {
            val personIdent = firstArg<HentPersonhistorikkRequest>().aktoer as PersonIdent
            if (SøknadTestdata.barnPersonident == personIdent.ident.ident) {
                hentPersonhistorikkResponseBarn()
            } else {
                hentPersonHistorikkResponse(personIdent.ident.ident == SøknadTestdata.morPersonident)
            }
        }
        every { personConsumer.hentPersonResponse(any()) } answers {
            val personIdent = firstArg<HentPersonRequest>().aktoer as PersonIdent
            when (personIdent.ident.ident) {
                SøknadTestdata.morPersonident -> hentPersonResponseForMor()
                SøknadTestdata.barnPersonident -> hentPersonResponseForBarn()
                else -> hentPersonResponseForFar()
            }
        }

        every { personConsumer.ping() } just Runs
        return personConsumer
    }

    companion object {

        private val TOM = LocalDate.now()
        private val FOM = TOM.minusYears(5)
        private val FOM_BARNET = LocalDate.of(2018, 5, 1)
        private val NORGE = Landkoder().withValue("NOR")
        private val NORSK_ADRESSE =
                Bostedsadresse().withStrukturertAdresse(Gateadresse().withGatenavn("Sannergata")
                                                                .withHusnummer(2)
                                                                .withPoststed(Postnummer().withValue("0560"))
                                                                .withLandkode(NORGE))
        private val MOR_PERSON_IDENT =
                PersonIdent().withIdent(NorskIdent().withIdent(SøknadTestdata.morPersonident))
        private val BARN_PERSON_IDENT =
                PersonIdent().withIdent(NorskIdent().withIdent(SøknadTestdata.barnPersonident))
        private val FAR_PERSON_IDENT =
                PersonIdent().withIdent(NorskIdent().withIdent(SøknadTestdata.farPersonident))

        private fun hentPersonHistorikkResponse(erMor: Boolean): HentPersonhistorikkResponse {
            val response = HentPersonhistorikkResponse()
            response.aktoer = if (erMor) MOR_PERSON_IDENT else FAR_PERSON_IDENT
            response.withStatsborgerskapListe(hentStatsborgerskap(false))
                    .withPersonstatusListe(hentPersonstatus(false))
                    .withMidlertidigAdressePeriodeListe(hentMidlertidigAdresse())
                    .withBostedsadressePeriodeListe(hentBostedsadresse(false))
            return response
        }

        private fun hentPersonhistorikkResponseBarn(): HentPersonhistorikkResponse {
            return HentPersonhistorikkResponse().withAktoer(BARN_PERSON_IDENT)
                    .withStatsborgerskapListe(hentStatsborgerskap(true))
                    .withBostedsadressePeriodeListe(hentBostedsadresse(true))
                    .withPersonstatusListe(hentPersonstatus(true))
        }

        private fun hentPersonResponseForMor(): HentPersonResponse {
            return HentPersonResponse().withPerson(hentPersoninfoMor())
        }

        private fun hentPersonResponseForBarn(): HentPersonResponse {
            return HentPersonResponse().withPerson(hentPersoninfoBarn())
        }

        private fun hentPersonResponseForFar(): HentPersonResponse {
            return HentPersonResponse().withPerson(hentPersoninfoFar())
        }

        private fun hentPersoninfoMor(): Person {
            return hentStandardPersoninfo()
                    .withKjoenn(Kjoenn().withKjoenn(Kjoennstyper().withValue("K")))
                    .withSivilstand(Sivilstand().withSivilstand(Sivilstander().withValue("GIFT")))
                    .withPersonnavn(Personnavn().withSammensattNavn("TEST TESTESEN"))
                    .withHarFraRolleI(hentFamilierelasjonerMor())
                    .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                    .withAktoer(MOR_PERSON_IDENT)
        }

        private fun hentPersoninfoFar(): Person {
            return hentStandardPersoninfo()
                    .withKjoenn(Kjoenn().withKjoenn(Kjoennstyper().withValue("M")))
                    .withSivilstand(Sivilstand().withSivilstand(Sivilstander().withValue("GIFT")))
                    .withPersonnavn(Personnavn().withSammensattNavn("EKTEMANN TESTESEN"))
                    .withHarFraRolleI(hentFamilierelasjonerFar())
                    .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                    .withAktoer(FAR_PERSON_IDENT)
        }

        private fun hentPersoninfoBarn(): Person {
            return hentStandardPersoninfo()
                    .withKjoenn(Kjoenn().withKjoenn(Kjoennstyper().withValue("K")))
                    .withSivilstand(Sivilstand().withSivilstand(Sivilstander().withValue("UGIF")))
                    .withPersonnavn(Personnavn().withSammensattNavn("BARN TESTESEN"))
                    .withHarFraRolleI(hentFamilierelasjonerBarn())
                    .withFoedselsdato(hentFoedselsdato("2018-05-01"))
                    .withAktoer(BARN_PERSON_IDENT)
        }

        private fun hentStandardPersoninfo(): Bruker {
            return Bruker()
                    .withPersonstatus(Personstatus().withPersonstatus(Personstatuser().withValue("BOSA")))
                    .withGeografiskTilknytning(Bydel().withGeografiskTilknytning("0315"))
                    .withGjeldendePostadressetype(Postadressetyper().withValue("BOSTEDSADRESSE"))
                    .withStatsborgerskap(Statsborgerskap().withLand(NORGE))
                    .withBostedsadresse(NORSK_ADRESSE)
        }

        private fun hentFamilierelasjonerMor(): Collection<Familierelasjon> {
            val giftMed = Familierelasjon()
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("EKTE"))
                    .withTilPerson(Person()
                                           .withAktoer(PersonIdent()
                                                               .withIdent(NorskIdent()
                                                                                  .withIdent(SøknadTestdata.farPersonident)))
                                           .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("EKTEMANN TESTESEN")))
            val barnet = Familierelasjon()
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("BARN"))
                    .withTilPerson(Person().withAktoer(BARN_PERSON_IDENT)
                                           .withFoedselsdato(hentFoedselsdato("2018-05-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("BARN TESTESEN")))
            return listOf(giftMed, barnet)
        }

        private fun hentFamilierelasjonerBarn(): Collection<Familierelasjon> {
            val far = Familierelasjon()
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("FARA"))
                    .withTilPerson(Person().withAktoer(PersonIdent()
                                                               .withIdent(NorskIdent()
                                                                                  .withIdent(SøknadTestdata.farPersonident)))
                                           .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("EKTEMANN TESTESEN")))
            val mor = Familierelasjon()
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("MORA"))
                    .withTilPerson(Person().withAktoer(MOR_PERSON_IDENT)
                                           .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("TEST TESTESEN")))
            return listOf(far, mor)
        }

        private fun hentFamilierelasjonerFar(): Collection<Familierelasjon> {
            val giftMed = Familierelasjon()
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("EKTE"))
                    .withTilPerson(Person().withAktoer(MOR_PERSON_IDENT)
                                           .withFoedselsdato(hentFoedselsdato("1990-01-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("TEST TESTESEN")))
            val barnet = Familierelasjon()
                    .withHarSammeBosted(true)
                    .withTilRolle(Familierelasjoner().withValue("BARN"))
                    .withTilPerson(Person().withAktoer(BARN_PERSON_IDENT)
                                           .withFoedselsdato(hentFoedselsdato("2018-05-01"))
                                           .withPersonnavn(Personnavn().withSammensattNavn("BARN TESTESEN")))
            return listOf(giftMed, barnet)
        }

        private fun hentFoedselsdato(dato: String): Foedselsdato {
            return try {
                Foedselsdato().withFoedselsdato(DatatypeFactory.newInstance().newXMLGregorianCalendar(dato))
            } catch (e: DatatypeConfigurationException) {
                throw IllegalStateException(e)
            }
        }

        private fun hentPersonstatus(erBarnet: Boolean): Collection<PersonstatusPeriode> {
            val personstatusPeriode = PersonstatusPeriode()
                    .withPersonstatus(Personstatuser().withValue("BOSA"))
                    .withPeriode(Periode().withFom(DateUtil.convertToXMLGregorianCalendar(if (erBarnet) FOM_BARNET else FOM))
                                         .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)))
            return listOf(personstatusPeriode)
        }

        private fun hentStatsborgerskap(erBarnet: Boolean): Collection<StatsborgerskapPeriode> {
            val statsborgerskapPeriode = StatsborgerskapPeriode()
                    .withStatsborgerskap(Statsborgerskap().withLand(NORGE))
                    .withPeriode(Periode().withFom(DateUtil.convertToXMLGregorianCalendar(if (erBarnet) FOM_BARNET else FOM))
                                         .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)))
            return listOf(statsborgerskapPeriode)
        }

        private fun hentBostedsadresse(erBarnet: Boolean): Collection<BostedsadressePeriode> {
            val bostedsadressePeriode = BostedsadressePeriode()
                    .withBostedsadresse(NORSK_ADRESSE)
                    .withPeriode(Periode().withFom(DateUtil.convertToXMLGregorianCalendar(if (erBarnet) FOM_BARNET else FOM))
                                         .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)))
            return listOf(bostedsadressePeriode)
        }

        private fun hentMidlertidigAdresse(): Collection<MidlertidigPostadresse> {
            val midlertidigPostadresseUtland = MidlertidigPostadresseUtland()
                    .withUstrukturertAdresse(UstrukturertAdresse().withAdresselinje1("TEST 1")
                                                     .withAdresselinje2("TEST 2")
                                                     .withAdresselinje3("TEST 3")
                                                     .withLandkode(Landkoder().withValue("SWE")))
                    .withPostleveringsPeriode(Gyldighetsperiode().withFom(DateUtil.convertToXMLGregorianCalendar(FOM))
                                                      .withTom(DateUtil.convertToXMLGregorianCalendar(TOM)))
            return listOf(midlertidigPostadresseUtland)
        }
    }
}