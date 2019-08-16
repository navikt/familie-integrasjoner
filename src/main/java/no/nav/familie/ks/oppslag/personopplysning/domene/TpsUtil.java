package no.nav.familie.ks.oppslag.personopplysning.domene;

import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;

public final class TpsUtil {

    private TpsUtil() {
        //for å hindre instanser av util klasse
    }

    public static String getPersonnavn(Person person) {
        return person.getPersonnavn().getSammensattNavn();
    }
}
