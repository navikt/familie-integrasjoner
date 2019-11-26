package no.nav.familie.integrasjoner;

class AccessTokenResponseTest {
    public String access_token;
    public String token_type;
    public Long expires_in;

    AccessTokenResponseTest(String access_token, String token_type, Long expires_in) {
        this.access_token = access_token;
        this.token_type = token_type;
        this.expires_in = expires_in;
    }
}
