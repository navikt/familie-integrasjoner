package no.nav.familie.ks.oppslag.egenansatt;

import no.nav.familie.ks.oppslag.egenansatt.internal.EgenAnsattConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EgenAnsattService {

    private EgenAnsattConsumer egenAnsattConsumer;

    @Autowired
    EgenAnsattService (EgenAnsattConsumer egenAnsattConsumer) {
        this.egenAnsattConsumer = egenAnsattConsumer;
    }

    public boolean erEgenAnsatt(String fnr) {
        return egenAnsattConsumer.erEgenAnsatt(fnr);
    }
}
