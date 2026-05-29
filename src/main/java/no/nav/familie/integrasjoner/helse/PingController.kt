package no.nav.familie.integrasjoner.helse

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ping")
class PingController {
    @GetMapping(produces = ["text/plain"])
    fun ping(): ResponseEntity<String> = ResponseEntity.ok("pong")
}
