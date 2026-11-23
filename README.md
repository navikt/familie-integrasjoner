# familie-integrasjoner
App for integrasjoner mot NAV-interne registre, GSAK, mm. Brukes i saksbehandling av kontantstøtte, barnetrygd og enslig forsørger.

## Bygging lokalt
Appen kjører på Java 25. Bygging gjøres ved å kjøre `mvn clean install`. 

## Kjøring og testing lokalt
For å kjøre opp appen lokalt kan en kjøre `DevLauncher` med Spring-profilen `dev` satt. Dette kan feks gjøres ved å sette
`-Dspring.profiles.active=dev` under Edit Configurations -> VM Options. 

Appen er da tilgjengelig under `localhost:8085`.

### Swagger
http://localhost:8085/swagger-ui/index.html


#### Test token i q2
I q2 er det mulig å hente ut azure test tokens fra azure-token-generator:

##### fss
client credential: https://azure-token-generator.intern.dev.nav.no/api/m2m?aud=dev-fss.teamfamilie.familie-integrasjoner

on-behalf-of: https://azure-token-generator.intern.dev.nav.no/api/obo?aud=dev-fss.teamfamilie.familie-integrasjoner

##### gcp
client credential: https://azure-token-generator.intern.dev.nav.no/api/m2m?aud=dev-gcp.teamfamilie.familie-integrasjoner

on-behalf-of: https://azure-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp.teamfamilie.familie-integrasjoner
