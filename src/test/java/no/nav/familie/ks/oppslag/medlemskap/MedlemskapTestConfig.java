package no.nav.familie.ks.oppslag.medlemskap;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.familie.ks.oppslag.medlemskap.internal.MedlClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Configuration
public class MedlemskapTestConfig {

    private static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Bean
    @Profile("mock-medlemskap")
    @Primary
    public MedlClient medlClientMock() throws Exception {
        MedlClient medlMock = mock(MedlClient.class);
        when(medlMock.hentMedlemskapsUnntakResponse(anyString())).thenReturn(mockMedlemskapResponse());
        doNothing().when(medlMock).ping();
        return medlMock;
    }

    private List<MedlemskapsUnntakResponse> mockMedlemskapResponse() {
        File medlemskapsResponseBody = new File(getFile());

        try {
            return Arrays.asList(mapper.readValue(medlemskapsResponseBody, MedlemskapsUnntakResponse[].class));
        } catch (IOException e) {
            throw new RuntimeException("Feil ved mapping av medl2-mock", e);
        }
    }

    private String getFile() {
        return getClass().getClassLoader().getResource("medlemskap/medlrespons.json").getFile();
    }
}
