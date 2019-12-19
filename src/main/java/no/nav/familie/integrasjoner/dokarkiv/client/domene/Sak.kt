package no.nav.familie.integrasjoner.dokarkiv.client.domene;

import javax.validation.constraints.NotNull;

public class Sak {
    @NotNull(message = "Sak mangler arkivsaksnummer")
    private String arkivsaksnummer;

    @NotNull(message = "Sak mangler arkivsaksystem")
    private String arkivsaksystem;
}
