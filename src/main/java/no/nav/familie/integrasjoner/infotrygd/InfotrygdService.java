package no.nav.familie.integrasjoner.infotrygd;

import no.nav.familie.integrasjoner.config.BaseService;
import no.nav.familie.integrasjoner.infotrygd.domene.AktivKontantstøtteInfo;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
public class InfotrygdService extends BaseService {

    private static final String OAUTH2_CLIENT_CONFIG_KEY = "infotrygd";

    private String infotrygdURL;

    @Autowired
    public InfotrygdService(RestTemplateBuilder restTemplateBuilderMedProxy,
                            ClientConfigurationProperties clientConfigurationProperties,
                            OAuth2AccessTokenService oAuth2AccessTokenService,
                            @Value("${INFOTRYGD_URL}") String infotrygdURL) {
        super(OAUTH2_CLIENT_CONFIG_KEY, restTemplateBuilderMedProxy, clientConfigurationProperties, oAuth2AccessTokenService);

        this.infotrygdURL = infotrygdURL;
    }

    public AktivKontantstøtteInfo hentAktivKontantstøtteFor(String fnr) {
        if (!fnr.matches("[0-9]+")) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "fnr må være et tall");
        }

        var headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("fnr", fnr);
        var entity = new HttpEntity(headers);

        var response = restTemplate.exchange(String.format("%s/v1/harBarnAktivKontantstotte", infotrygdURL),
                                             HttpMethod.GET,
                                             entity,
                                             AktivKontantstøtteInfo.class);

        if (response.getBody() != null && response.getBody().getHarAktivKontantstotte() != null) {
            return response.getBody();
        }
        throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Ufullstendig eller tom respons.");
    }

    public void ping() {
        restTemplate.getForEntity(String.format("%s/actuator/health", infotrygdURL), String.class);
    }
}
