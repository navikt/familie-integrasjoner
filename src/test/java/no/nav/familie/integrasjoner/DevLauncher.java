package no.nav.familie.integrasjoner;

import no.nav.familie.integrasjoner.config.ApplicationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@Import({ApplicationConfig.class})
public class DevLauncher {

    public static void main(String... args) {
        SpringApplication app = new SpringApplicationBuilder(ApplicationConfig.class)
                .profiles("dev",
                          "mock-aktor",
                          "mock-dokarkiv",
                          "mock-egenansatt",
                          "mock-infotrygd",
                          "mock-medlemskap",
                          "mock-oppgave",
                          "mock-personopplysninger",
                          "mock-saf",
                          "mock-sts",
                          "mock-kodeverk",
                          "mock-arbeidsfordeling",
                          "mock-pdl"
                ).build();
        app.run(args);
    }
}
