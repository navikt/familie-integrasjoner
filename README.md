# familie-ks-oppslag
App for oppslag mot NAV-interne registre, GSAK, infotrygd mm. Brukes i saksbehandling av kontantstøtte-søknader.

## Bygging lokalt
Appen kjører på Java 11. Bygging gjøres ved å kjøre `mvn clean install`. 

## Kjøring og testing lokalt
For å kjøre opp appen lokalt kan en kjøre `DevLauncher` med Spring-profilen `dev` satt. Dette kan feks gjøres ved å sette
`-Dspring.profiles.active=dev` under Edit Configurations -> VM Options. 

Appen er da tilgjengelig under `localhost:8085`.

Dersom man vil gå mot endepunkter som krever autentisering lokalt, kan man få et testtoken ved å gå mot `localhost:8085/local/cookie`. 


## Produksjonssetting
Hvis du skal deploye appen til prod, må du pushe en ny tag på master. Dette gjøres ved å kjøre tag-scriptet som ligger i `.github`-mappen. Da spesifiserer du om du vil bumpe major eller minor, scriptet vil da bumpe med 1 opp fra nyeste tag. 

Eksempelvis: 

Nyeste tag er `v0.5`.`./tag.sh -M` vil da pushe tagen `v1.0`, og `./tag.sh -m` vil pushe tagen `v0.6`.

Når en ny tag pushes, trigges github action workflowen som heter Deploy-Prod. 