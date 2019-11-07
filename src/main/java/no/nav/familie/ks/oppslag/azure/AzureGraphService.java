package no.nav.familie.ks.oppslag.azure;

import no.nav.familie.ks.oppslag.azure.domene.Saksbehandler;
import no.nav.familie.ks.oppslag.config.BaseService;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
public class AzureGraphService extends BaseService {

    private static final String OAUTH2_CLIENT_CONFIG_KEY = "aad-graph-onbehalfof";
    private String aadGrapURI;

    @Autowired
    public AzureGraphService(RestTemplateBuilder restTemplateBuilderMedProxy,
                             ClientConfigurationProperties clientConfigurationProperties,
                             OAuth2AccessTokenService oAuth2AccessTokenService,
                             @Value("${AAD_GRAPH_API_URI}") String URI) {
        super(OAUTH2_CLIENT_CONFIG_KEY, restTemplateBuilderMedProxy, clientConfigurationProperties, oAuth2AccessTokenService);

        this.aadGrapURI = URI;
    }

    public Saksbehandler getSaksbehandler() {

        var headers = new HttpHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");
        var entity = new HttpEntity(headers);

        var response = restTemplate.exchange(String.format("%sme?$select=displayName,onPremisesSamAccountName,userPrincipalName", aadGrapURI), HttpMethod.GET, entity, Saksbehandler.class);

        return response.getBody();
    }
}
