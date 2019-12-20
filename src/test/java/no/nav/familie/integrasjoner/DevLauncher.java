package no.nav.familie.integrasjoner;

import no.nav.familie.integrasjoner.config.ApplicationConfig;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@EnableSwagger2
@Import({ApplicationConfig.class, TokenGeneratorConfiguration.class})
public class DevLauncher {

    public static void main(String... args) {
        SpringApplication app = new SpringApplicationBuilder(ApplicationConfig.class)
                .profiles("dev",
                        "mock-aktor",
                        "mock-dokarkiv",
                        "mock-egenansatt",
                        "mock-infotrygd",
                        "mock-innsyn",
                        "mock-medlemskap",
                        "mock-oppgave",
                        "mock-personopplysninger",
                        "mock-saf",
                        "mock-sts",
                        "mock-kodeverk"
                ).build();
        app.run(args);
    }
}
