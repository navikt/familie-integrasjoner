package no.nav.familie.ks.oppslag;

import no.nav.familie.ks.oppslag.config.ApplicationConfig;
import no.nav.familie.ks.oppslag.config.DelayedShutdownHook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class Launcher {

    public static void main(String... args) {
        SpringApplication app = new SpringApplicationBuilder(ApplicationConfig.class)
                .build();
        app.setRegisterShutdownHook(false);
        ConfigurableApplicationContext applicationContext = app.run(args);
        Runtime.getRuntime().addShutdownHook(new DelayedShutdownHook(applicationContext));
    }

}
