package no.nav.familie.ks.oppslag.personopplysning;

import no.nav.familie.ks.oppslag.DevLauncher;
import no.nav.familie.ks.oppslag.personopplysning.domene.Personinfo;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DevLauncher.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"dev", "mock-aktor", "mock-personopplysninger"})
public class PersonopplysningControllerTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    private HttpHeaders headers = new HttpHeaders();

    @Ignore
    @Test
    public void testHttpResponseStub() {

        ResponseEntity<String> cookie = restTemplate.exchange(
                url("/local/cookie"), HttpMethod.GET, new HttpEntity<String>(null, headers), String.class
        );
        headers.add("Authorization", "Bearer " + tokenFraRespons(cookie));

        ResponseEntity<Personinfo> response = restTemplate.exchange(
                url("/api/personopplysning/info?id=1"), HttpMethod.GET, new HttpEntity<String>(null, headers), Personinfo.class
        );

    }

    private String tokenFraRespons(ResponseEntity<String> cookie) {
        return cookie.getBody().split("value\":\"")[1].split("\"")[0];
    }

    private String url(String uri) {
        return "http://localhost:" + port + uri;
    }
}

