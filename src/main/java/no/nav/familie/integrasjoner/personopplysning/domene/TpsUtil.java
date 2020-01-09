package no.nav.familie.integrasjoner.personopplysning.domene;

import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;

public final class TpsUtil {

    private TpsUtil() {
        //for å hindre instanser av util klasse
    }

    public static String getPersonnavn(Person person) {
        return person.getPersonnavn().getSammensattNavn();
    }
}
