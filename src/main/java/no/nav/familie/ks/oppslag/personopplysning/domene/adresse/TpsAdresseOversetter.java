package no.nav.familie.ks.oppslag.personopplysning.domene.adresse;

import no.nav.familie.ks.oppslag.felles.ws.DateUtil;
import no.nav.familie.ks.oppslag.personopplysning.domene.Periode;
import no.nav.familie.ks.oppslag.personopplysning.domene.PersonhistorikkInfo;
import no.nav.familie.ks.oppslag.personopplysning.domene.TpsUtil;
import no.nav.familie.ks.oppslag.personopplysning.domene.status.PersonstatusType;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Component
public class TpsAdresseOversetter {

    private static final String NORGE = "NOR";
    private static final String HARDKODET_POSTNR = "XXXX";
    private static final String HARDKODET_POSTSTED = "UDEFINERT";
    private static final String POSTNUMMER_POSTSTED = "^\\d{4} \\D*";  // Mønster for postnummer og poststed, f.eks. "0034 OSLO"


    public List<Adresseinfo> lagListeMedAdresseInfo(Bruker person) {
        Optional<AdresseType> gjeldende = finnGjeldendePostadressetype(person);
        if (gjeldende.isPresent() && Objects.equals(AdresseType.UKJENT_ADRESSE, gjeldende.get())) {
            return Collections.singletonList(byggUkjentAdresse(person));
        }

        List<Adresseinfo> adresseInfoList = new ArrayList<>();
        if (person.getBostedsadresse() != null) {
            StrukturertAdresse adresseStruk = person.getBostedsadresse().getStrukturertAdresse();
            adresseInfoList.add(konverterStrukturertAdresse(person, adresseStruk, AdresseType.BOSTEDSADRESSE));
        }
        if (person.getPostadresse() != null) {
            UstrukturertAdresse adresseUstruk = person.getPostadresse().getUstrukturertAdresse();
            adresseInfoList.add(konverterUstrukturertAdresse(person, adresseUstruk, AdresseType.POSTADRESSE));
            Landkoder landkode = adresseUstruk.getLandkode();
            if (NORGE.equals(landkode.getValue())) {
                adresseInfoList.add(konverterUstrukturertAdresse(person, adresseUstruk, AdresseType.POSTADRESSE));
            } else {
                adresseInfoList.add(konverterUstrukturertAdresse(person, adresseUstruk, AdresseType.POSTADRESSE_UTLAND));
            }
        }
        if (person.getMidlertidigPostadresse() != null) {
            if (person.getMidlertidigPostadresse() instanceof MidlertidigPostadresseNorge) {
                StrukturertAdresse adresseStruk = ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse();
                adresseInfoList.add(konverterStrukturertAdresse(person, adresseStruk, AdresseType.MIDLERTIDIG_POSTADRESSE_NORGE));
            } else if (person.getMidlertidigPostadresse() instanceof MidlertidigPostadresseUtland) {
                UstrukturertAdresse adresseUstruk = ((MidlertidigPostadresseUtland) person.getMidlertidigPostadresse()).getUstrukturertAdresse();
                adresseInfoList.add(konverterUstrukturertAdresse(person, adresseUstruk, AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND));
            }
        }
        return adresseInfoList;
    }

    public void konverterBostedadressePerioder(HentPersonhistorikkResponse response, PersonhistorikkInfo.Builder builder) {
        if (Optional.ofNullable(response.getBostedsadressePeriodeListe()).isPresent()) {
            response.getBostedsadressePeriodeListe().forEach(e -> {
                StrukturertAdresse strukturertAdresse = e.getBostedsadresse().getStrukturertAdresse();
                Periode periode =
                        Periode
                                .innenfor(DateUtil.convertToLocalDate(e.getPeriode().getFom()), DateUtil.convertToLocalDate(e.getPeriode().getTom()));

                AdressePeriode adressePeriode = konverterStrukturertAdresse(strukturertAdresse, AdresseType.BOSTEDSADRESSE, periode);
                builder.leggTil(adressePeriode);
            });
        }
    }

    public void konverterPostadressePerioder(HentPersonhistorikkResponse response, PersonhistorikkInfo.Builder builder) {
        if (Optional.ofNullable(response.getPostadressePeriodeListe()).isPresent()) {
            response.getPostadressePeriodeListe().forEach(e -> {
                Periode periode =
                        Periode
                                .innenfor(DateUtil.convertToLocalDate(e.getPeriode().getFom()), DateUtil.convertToLocalDate(e.getPeriode().getTom()));

                UstrukturertAdresse ustrukturertAdresse = e.getPostadresse().getUstrukturertAdresse();
                Landkoder landkode = ustrukturertAdresse.getLandkode();
                if (NORGE.equals(landkode.getValue())) {
                    AdressePeriode adressePeriode = konverterUstrukturertAdresse(ustrukturertAdresse, AdresseType.POSTADRESSE, periode);
                    builder.leggTil(adressePeriode);
                } else {
                    AdressePeriode adressePeriode = konverterUstrukturertAdresse(ustrukturertAdresse, AdresseType.POSTADRESSE_UTLAND, periode);
                    builder.leggTil(adressePeriode);
                }
            });
        }
    }

    public void konverterMidlertidigAdressePerioder(HentPersonhistorikkResponse response, PersonhistorikkInfo.Builder builder) {
        if (Optional.ofNullable(response.getMidlertidigAdressePeriodeListe()).isPresent()) {
            response.getMidlertidigAdressePeriodeListe().forEach(e -> {
                Periode periode =
                        Periode
                                .innenfor(DateUtil.convertToLocalDate(e.getPostleveringsPeriode().getFom()), DateUtil.convertToLocalDate(e.getPostleveringsPeriode().getTom()));
                if (e instanceof MidlertidigPostadresseNorge) {
                    StrukturertAdresse strukturertAdresse = ((MidlertidigPostadresseNorge) e).getStrukturertAdresse();
                    AdressePeriode adressePeriode = konverterStrukturertAdresse(strukturertAdresse, AdresseType.MIDLERTIDIG_POSTADRESSE_NORGE, periode);
                    builder.leggTil(adressePeriode);
                } else if (e instanceof MidlertidigPostadresseUtland) {
                    UstrukturertAdresse ustrukturertAdresse = ((MidlertidigPostadresseUtland) e).getUstrukturertAdresse();
                    AdressePeriode adressePeriode = konverterUstrukturertAdresse(ustrukturertAdresse, AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND, periode);
                    builder.leggTil(adressePeriode);
                }
            });
        }
    }

    private Adresseinfo finnGjeldendeAdresseFor(Bruker bruker) {
        Optional<AdresseType> gjeldende = finnGjeldendePostadressetype(bruker);
        if (gjeldende.isPresent()) {
            if (AdresseType.BOSTEDSADRESSE.equals(gjeldende.get())) {
                return konverterStrukturertAdresse(bruker, bruker.getBostedsadresse().getStrukturertAdresse(), gjeldende.get());
            } else if (AdresseType.POSTADRESSE.equals(gjeldende.get()) || AdresseType.POSTADRESSE_UTLAND.equals(gjeldende.get())) {
                return konverterUstrukturertAdresse(bruker, bruker.getPostadresse().getUstrukturertAdresse(), gjeldende.get());
            } else if (AdresseType.MIDLERTIDIG_POSTADRESSE_NORGE.equals(gjeldende.get())) {
                return konverterMidlertidigPostadresseNorge(bruker, gjeldende.get());
            } else if (AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND.equals(gjeldende.get())) {
                return konverterMidlertidigPostadresseUtland(bruker, gjeldende.get());
            } else if (AdresseType.UKJENT_ADRESSE.equals(gjeldende.get())) {
                return byggUkjentAdresse(bruker);
            }
        }
        throw new IllegalArgumentException("Ukjent adressetype : " + bruker.getGjeldendePostadressetype().getValue());
    }

    private Adresseinfo konverterMidlertidigPostadresseNorge(Bruker bruker,
                                                             AdresseType gjeldende) {
        StrukturertAdresse midlertidigAdresse = ((MidlertidigPostadresseNorge) bruker.getMidlertidigPostadresse()).getStrukturertAdresse();
        return konverterStrukturertAdresse(bruker, midlertidigAdresse, gjeldende);
    }

    private Adresseinfo konverterMidlertidigPostadresseUtland(Bruker bruker,
                                                              AdresseType gjeldende) {
        UstrukturertAdresse midlertidigAdresse = ((MidlertidigPostadresseUtland) bruker.getMidlertidigPostadresse()).getUstrukturertAdresse();
        return konverterUstrukturertAdresse(bruker, midlertidigAdresse, gjeldende);
    }

    Optional<AdresseType> finnGjeldendePostadressetype(Bruker bruker) {
        return Arrays.stream(AdresseType.values()).filter(it -> it.name().equals(bruker.getGjeldendePostadressetype().getValue())).findFirst();
    }

    Adresseinfo konverterStrukturertAdresse(Bruker bruker,
                                            StrukturertAdresse adresse,
                                            AdresseType adresseType) {
        requireNonNull(adresse);
        if (adresse instanceof Gateadresse) {
            return konverterStrukturertAdresse(bruker, adresseType, (Gateadresse) adresse);
        } else if (adresse instanceof Matrikkeladresse) {
            return konverterStrukturertAdresse(bruker, adresseType, (Matrikkeladresse) adresse);
        } else if (adresse instanceof PostboksadresseNorsk) {
            return konverterStrukturertAdresse(bruker, adresseType, (PostboksadresseNorsk) adresse);
        } else if (adresse instanceof StedsadresseNorge) {
            return konverterStrukturertAdresse(bruker, adresseType, (StedsadresseNorge) adresse);
        } else {
            throw new IllegalArgumentException("Ikke-støttet klasse for strukturert adresse: " + adresse.getClass());
        }
    }

    private AdressePeriode konverterStrukturertAdresse(StrukturertAdresse adresse, AdresseType adresseType, Periode periode) {
        requireNonNull(adresse);
        requireNonNull(adresseType);
        requireNonNull(periode);

        Adresse strukturertAdresse;
        if (adresse instanceof Gateadresse) {
            strukturertAdresse = konverterStrukturertAdresse((Gateadresse) adresse);
        } else if (adresse instanceof Matrikkeladresse) {
            strukturertAdresse = konverterStrukturertAdresse((Matrikkeladresse) adresse);
        } else if (adresse instanceof PostboksadresseNorsk) {
            strukturertAdresse = konverterStrukturertAdresse((PostboksadresseNorsk) adresse);
        } else if (adresse instanceof StedsadresseNorge) {
            strukturertAdresse = konverterStrukturertAdresse((StedsadresseNorge) adresse);
        } else {
            throw new IllegalArgumentException("Ikke-støttet klasse for strukturert adresse: " + adresse.getClass());
        }

        return byggAdressePeriode(adresseType, strukturertAdresse, periode);
    }

    private AdressePeriode konverterUstrukturertAdresse(UstrukturertAdresse ustrukturertAdresse, AdresseType adresseType, Periode periode) {

        Adresse adresse = konverterUstrukturertAdresse(ustrukturertAdresse);

        return byggAdressePeriode(adresseType, adresse, periode);
    }

    private Adresseinfo konverterStrukturertAdresse(Bruker bruker,
                                                    AdresseType gjeldende,
                                                    Matrikkeladresse matrikkeladresse) {

        Adresse adresse = konverterStrukturertAdresse(matrikkeladresse);

        return byggAddresseinfo(bruker, gjeldende, adresse);
    }

    private String adresseFraBolignummerOgEiendomsnavn(Matrikkeladresse matrikkeladresse) {
        return matrikkeladresse.getBolignummer() == null ? matrikkeladresse.getEiendomsnavn() : matrikkeladresseMedBolignummer(matrikkeladresse);
    }

    private String matrikkeladresseMedBolignummer(Matrikkeladresse matrikkeladresse) {
        return "Bolignummer " + matrikkeladresse.getBolignummer() + " " + matrikkeladresse.getEiendomsnavn();
    }

    private Adresseinfo konverterStrukturertAdresse(Bruker bruker,
                                                    AdresseType gjeldende,
                                                    Gateadresse gateadresse) {

        Adresse adresse = konverterStrukturertAdresse(gateadresse);
        return byggAddresseinfo(bruker, gjeldende, adresse);
    }

    private String adresseFraGateadresse(Gateadresse gateadresse) {
        return gateadresse.getGatenavn() +
                hvisfinnes(gateadresse.getHusnummer()) +
                hvisfinnes(gateadresse.getHusbokstav());
    }

    Adresseinfo byggUkjentAdresse(Bruker bruker) {
        return new Adresseinfo.Builder(AdresseType.UKJENT_ADRESSE,
                bruker.getPersonnavn().getSammensattNavn(),
                tilPersonstatusType(bruker.getPersonstatus())).build();
    }

    private Adresseinfo konverterStrukturertAdresse(Bruker bruker,
                                                    AdresseType gjeldende,
                                                    StedsadresseNorge stedsadresseNorge) {

        Adresse adresse = konverterStrukturertAdresse(stedsadresseNorge);
        return byggAddresseinfo(bruker, gjeldende, adresse);
    }

    private Adresseinfo konverterStrukturertAdresse(Bruker bruker,
                                                    AdresseType gjeldende,
                                                    PostboksadresseNorsk postboksadresseNorsk) {
        Adresse adresse = konverterStrukturertAdresse(postboksadresseNorsk);
        return byggAddresseinfo(bruker, gjeldende, adresse);
    }

    private Adresseinfo.Builder adresseBuilderForPerson(Bruker bruker,
                                                        AdresseType gjeldende) {
        Personstatus personstatus = bruker.getPersonstatus();
        return new Adresseinfo.Builder(gjeldende,
                TpsUtil.getPersonnavn(bruker),
                personstatus == null ? null : tilPersonstatusType(personstatus));
    }

    private String postboksadresselinje(PostboksadresseNorsk postboksadresseNorsk) {
        return "Postboks" + hvisfinnes(postboksadresseNorsk.getPostboksnummer()) +
                hvisfinnes(postboksadresseNorsk.getPostboksanlegg());
    }

    Adresseinfo konverterUstrukturertAdresse(Bruker bruker,
                                             UstrukturertAdresse ustrukturertAdresse,
                                             AdresseType gjeldende) {

        Adresse adresse = konverterUstrukturertAdresse(ustrukturertAdresse);
        return byggAddresseinfo(bruker, gjeldende, adresse);
    }

    private Adresseinfo byggAddresseinfo(Bruker bruker, AdresseType gjeldende, Adresse adresse) {
        Adresseinfo adresseinfo = adresseBuilderForPerson(bruker, gjeldende)
                .medPostNr(adresse.postnummer)
                .medPoststed(adresse.poststed)
                .medLand(adresse.land)
                .medAdresselinje1(adresse.adresselinje1)
                .medAdresselinje2(adresse.adresselinje2)
                .medAdresselinje3(adresse.adresselinje3)
                .medAdresselinje4(adresse.adresselinje4)
                .build();
        return adresseinfo;
    }

    private AdressePeriode byggAdressePeriode(AdresseType adresseType, Adresse adresse, Periode periode) {
        return AdressePeriode.builder()
                .medGyldighetsperiode(periode)
                .medAdresselinje1(adresse.adresselinje1)
                .medAdresselinje2(adresse.adresselinje2)
                .medAdresselinje3(adresse.adresselinje3)
                .medAdresselinje4(adresse.adresselinje4)
                .medAdresseType(adresseType)
                .medLand(adresse.land)
                .medPostnummer(adresse.postnummer)
                .medPoststed(adresse.poststed)
                .build();
    }

    private Adresse konverterStrukturertAdresse(Gateadresse gateadresse) {

        String postnummer = Optional.ofNullable(gateadresse.getPoststed()).map(Kodeverdi::getValue).orElse(HARDKODET_POSTNR);

        Adresse adresse = new Adresse();
        adresse.postnummer = postnummer;
        adresse.poststed = tilPoststed(postnummer);
        adresse.land = tilLand(gateadresse.getLandkode());

        if (gateadresse.getTilleggsadresse() == null) {
            adresse.adresselinje1 = adresseFraGateadresse(gateadresse);
        } else {
            adresse.adresselinje1 = gateadresse.getTilleggsadresse();
            adresse.adresselinje2 = adresseFraGateadresse(gateadresse);
        }
        return adresse;
    }

    private Adresse konverterStrukturertAdresse(Matrikkeladresse matrikkeladresse) {
        Adresse adresse = new Adresse();
        adresse.postnummer = matrikkeladresse.getPoststed().getValue();
        adresse.poststed = tilPoststed(adresse.postnummer);

        if (matrikkeladresse.getLandkode() != null) {
            adresse.land = matrikkeladresse.getLandkode().getValue();
        }

        if (matrikkeladresse.getTilleggsadresse() == null) {
            adresse.adresselinje1 = adresseFraBolignummerOgEiendomsnavn(matrikkeladresse);
        } else {
            adresse.adresselinje1 = matrikkeladresse.getTilleggsadresse();
            adresse.adresselinje2 = adresseFraBolignummerOgEiendomsnavn(matrikkeladresse);
        }
        return adresse;
    }

    private Adresse konverterStrukturertAdresse(PostboksadresseNorsk postboksadresseNorsk) {
        Adresse adresse = new Adresse();
        adresse.postnummer = postboksadresseNorsk.getPoststed().getValue();
        adresse.poststed = tilPoststed(adresse.postnummer);
        adresse.land = tilLand(postboksadresseNorsk.getLandkode());

        if (postboksadresseNorsk.getTilleggsadresse() == null) {
            adresse.adresselinje1 = postboksadresselinje(postboksadresseNorsk);
        } else {
            adresse.adresselinje1 = postboksadresseNorsk.getTilleggsadresse();
            adresse.adresselinje2 = postboksadresselinje(postboksadresseNorsk);
        }

        return adresse;
    }

    private Adresse konverterStrukturertAdresse(StedsadresseNorge stedsadresseNorge) {

        Adresse adresse = new Adresse();
        adresse.postnummer = stedsadresseNorge.getPoststed().getValue();
        adresse.poststed = tilPoststed(adresse.postnummer);
        adresse.land = tilLand(stedsadresseNorge.getLandkode());
        adresse.adresselinje1 = stedsadresseNorge.getBolignummer();
        adresse.adresselinje2 = stedsadresseNorge.getTilleggsadresse();

        return adresse;
    }

    private Adresse konverterUstrukturertAdresse(UstrukturertAdresse ustrukturertAdresse) {
        Adresse adresse = new Adresse();
        adresse.adresselinje1 = ustrukturertAdresse.getAdresselinje1();
        adresse.adresselinje2 = ustrukturertAdresse.getAdresselinje2();
        adresse.adresselinje3 = ustrukturertAdresse.getAdresselinje3();
        adresse.land = tilLand(ustrukturertAdresse.getLandkode());


        String linje4 = ustrukturertAdresse.getAdresselinje4();
        // Ustrukturert adresse kan ha postnr + poststed i adresselinje4
        if (linje4 != null && linje4.matches(POSTNUMMER_POSTSTED)) {
            adresse.postnummer = linje4.substring(0, 4);
            adresse.poststed = linje4.substring(5);
        } else {
            adresse.adresselinje4 = linje4;
        }
        return adresse;
    }

    public String finnAdresseLandkodeFor(Bruker bruker) {
        Adresseinfo adresseinfo = tilAdresseInfo(bruker);
        return adresseinfo.getLand();
    }

    public String finnUtlandsadresseFor(Bruker bruker) {
        MidlertidigPostadresse midlertidigPostadresse = bruker.getMidlertidigPostadresse();
        if (midlertidigPostadresse instanceof MidlertidigPostadresseUtland) {
            MidlertidigPostadresseUtland postadresseUtland = (MidlertidigPostadresseUtland) midlertidigPostadresse;
            return byggOppAdresse(konverterUstrukturertAdresse(bruker,
                    postadresseUtland.getUstrukturertAdresse(),
                    AdresseType.MIDLERTIDIG_POSTADRESSE_UTLAND));
        }
        return null;
    }

    public String finnAdresseFor(Person person) {
        if (person instanceof Bruker) {
            Adresseinfo adresseinfo = tilAdresseInfo(person);
            return byggOppAdresse(adresseinfo);
        }
        return "UDEFINERT ADRESSE";
    }

    public Adresseinfo finnBostedsadresseFor(Person person) {
        Bruker bruker = (Bruker) person;
        Bostedsadresse bostedsadresse = person.getBostedsadresse();

        if (bostedsadresse == null) {
            return null;
        }

        StrukturertAdresse adresse = bostedsadresse.getStrukturertAdresse();
        return konverterStrukturertAdresse(bruker, adresse, AdresseType.BOSTEDSADRESSE);
    }

    private String byggOppAdresse(Adresseinfo adresseinfo) {
        String linje1 = adresseinfo.getAdresselinje1();
        String linje2 = Optional.ofNullable(adresseinfo.getAdresselinje2()).map(linje -> "\n" + linje).orElse("");
        String linje3 = Optional.ofNullable(adresseinfo.getAdresselinje3()).map(linje -> "\n" + linje).orElse("");
        String linje4 = Optional.ofNullable(adresseinfo.getAdresselinje4()).map(linje -> "\n" + linje).orElse("");
        String postnr = Optional.ofNullable(adresseinfo.getPostNr()).map(nr -> "\n" + nr).orElse("");
        String poststed = Optional.ofNullable(adresseinfo.getPoststed()).map(sted -> " " + sted).orElse("");
        String land = Optional.ofNullable(adresseinfo.getLand()).map(landKode -> "\n" + landKode).orElse("");
        return linje1 + linje2 + linje3 + linje4 + postnr + poststed + land;
    }

    Adresseinfo tilAdresseInfo(Person person) {
        if (person instanceof Bruker) {
            return finnGjeldendeAdresseFor((Bruker) person);
        }
        throw new IllegalArgumentException("Ukjent brukertype " + person);
    }

    private String tilPoststed(String postnummer) {
        InputStream postnummerFil = getPostnummerFil();
        BufferedReader bf = new BufferedReader(new InputStreamReader(postnummerFil));
        try (Stream<String> filLinje = bf.lines()) {
            String[][] allePostnumre = filLinje.map(s -> s.split("\t", 5))
                    .toArray(String[][]::new);

            for (String[] linje : allePostnumre) {
                if (postnummer.equals(linje[0])) {
                    return linje[1];
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Feil ved mapping av postnummer", e);
        }
        return HARDKODET_POSTSTED;
    }

    private InputStream getPostnummerFil() {
        return getClass().getClassLoader().getResourceAsStream("kodeverk/postnummer.txt");
    }

    private String tilLand(Landkoder landkoder) {
        return null == landkoder ? null : landkoder.getValue();
    }

    private PersonstatusType tilPersonstatusType(Personstatus personstatus) {
        return PersonstatusType.valueOf(personstatus.getPersonstatus().getValue());
    }

    private String hvisfinnes(Object object) {
        return object == null ? "" : " " + object.toString().trim();
    }

    private class Adresse {

        String adresselinje1;
        String adresselinje2;
        String adresselinje3;
        String adresselinje4;
        String postnummer;
        String poststed;
        String land;
    }
}
