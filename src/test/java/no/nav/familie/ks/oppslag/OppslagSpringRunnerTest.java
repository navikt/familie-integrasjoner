package no.nav.familie.ks.oppslag;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DevLauncher.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class OppslagSpringRunnerTest {

    private static final String LOCALHOST = "http://localhost:";

    protected ListAppender<ILoggingEvent> listAppender = initLoggingEventListAppender();
    protected List<ILoggingEvent> loggingEvents = listAppender.list;

    protected TestRestTemplate restTemplate = new TestRestTemplate();
    protected HttpHeaders headers = new HttpHeaders();

    @LocalServerPort
    private int port;

    @After
    public void reset() {
        loggingEvents.clear();
    }

    protected String getPort() {
        return String.valueOf(port);
    }

    protected String localhost(String uri) {
        return LOCALHOST + getPort() + uri;
    }

    protected String url(String baseUrl, String uri) {
        return baseUrl + uri;
    }

    protected String getLokalTestToken() {
        var cookie = restTemplate.exchange(localhost("/local/cookie"), HttpMethod.GET, HttpEntity.EMPTY, String.class);
        return tokenFraRespons(cookie);
    }

    private String tokenFraRespons(ResponseEntity<String> cookie) {
        return cookie.getBody().split("value\":\"")[1].split("\"")[0];
    }

    protected static ListAppender<ILoggingEvent> initLoggingEventListAppender() {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        return listAppender;
    }
}

