package no.nav.familie.integrasjoner;

import no.nav.familie.integrasjoner.config.ApplicationConfig;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@Import({ApplicationConfig.class, TokenGeneratorConfiguration.class})
@Profile("integrasjonstest")
public class UnitTestLauncher {

    public static void main(String... args) {
        SpringApplication app = new SpringApplicationBuilder(ApplicationConfig.class)
                .profiles("integrasjonstest",
                          "mock-aktor",
                          "mock-dokarkiv",
                          "mock-dokdist",
                          "mock-egenansatt",
                          "mock-infotrygd",
                          "mock-infotrygdsak",
                          "mock-medlemskap",
                          "mock-oppgave",
                          "mock-personopplysninger",
                          "mock-saf",
                          "mock-sts",
                          "mock-pdl"
                ).build();
        app.run(args);
    }
}