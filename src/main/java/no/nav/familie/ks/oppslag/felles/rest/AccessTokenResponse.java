package no.nav.familie.ks.oppslag.felles.rest;

class AccessTokenResponse {
    private String access_token;
    private String token_type;
    private Long expires_in;

    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public Long getExpires_in() {
        return expires_in;
    }
}
