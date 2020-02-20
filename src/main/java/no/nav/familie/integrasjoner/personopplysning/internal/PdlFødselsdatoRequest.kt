package no.nav.familie.integrasjoner.personopplysning.internal

data class PdlFødselsdatoRequest (val variables: PdlRequestVariable,
                                  val query: String = "query(\$ident: ID!) {person: hentPerson(ident: \$ident) {foedsel {foedselsdato}}}")