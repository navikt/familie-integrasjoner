package no.nav.familie.ks.oppslag.egenansatt.internal;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1;
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest;


public class EgenAnsattConsumer {

    private EgenAnsattV1 egenAnsattV1;
    private final Timer egenAnsattResponsTid = Metrics.timer("EgenAnsattV1.respons.tid");
    private final Counter egenAnsattSuccess = Metrics.counter("EgenAnsattV1.response", "status", "success");
    private final Counter egenAnsattFailure = Metrics.counter("EgenAnsattV1.response", "status", "failure");

    public EgenAnsattConsumer(EgenAnsattV1 port) {
        this.egenAnsattV1 = port;
    }

    public boolean erEgenAnsatt(String fnr) {
        return egenAnsattV1.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(new WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest()
                                                                               .withIdent(fnr)
        ).isEgenAnsatt();
    }

    public void ping() {
        egenAnsattV1.ping();
    }
}
