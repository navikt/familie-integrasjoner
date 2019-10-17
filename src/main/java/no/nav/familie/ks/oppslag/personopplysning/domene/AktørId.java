package no.nav.familie.ks.oppslag.personopplysning.domene;

import javax.validation.constraints.Pattern.Flag;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Id som genereres fra NAV Aktør Register. Denne iden benyttes til interne forhold i Nav og vil ikke endres f.eks. dersom bruker går fra
 * DNR til FNR i Folkeregisteret. Tilsvarende vil den kunne referere personer som har ident fra et utenlandsk system.
 */
public class AktørId {
    private static final String CHARS = "a-z0-9_:-";

    private static final String VALID_REGEXP = "^(-?[1-9]|[a-z0])[" + CHARS + "]*$";
    private static final String INVALID_REGEXP = "[^" + CHARS + "]+";

    private static final Pattern VALID = Pattern.compile(VALID_REGEXP, Pattern.CASE_INSENSITIVE);
    private static final Pattern INVALID = Pattern.compile(INVALID_REGEXP, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    @javax.validation.constraints.Pattern(regexp = VALID_REGEXP, flags = {Flag.CASE_INSENSITIVE})
    private String aktørId;

    public AktørId(String aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        if (!VALID.matcher(aktørId).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ugyldig aktørId, støtter kun A-Z/0-9/:/-/_ tegn. Var: " + aktørId.replaceAll(INVALID.pattern(), "?") + " (vasket)");
        }
        this.aktørId = aktørId;
    }

    public String getId() {
        return aktørId;
    }

}
