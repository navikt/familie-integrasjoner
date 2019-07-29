package no.nav.familie.ks.oppslag;

import no.nav.familie.ks.oppslag.config.ApplicationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;


@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@Import({ApplicationConfig.class, TokenGeneratorConfiguration.class})
public class DevLauncher {

    public static void main(String... args) {
        SpringApplication app = new SpringApplicationBuilder(ApplicationConfig.class)
                .profiles("dev", "mock-aktor")
                .build();
        app.run(args);
    }
}
