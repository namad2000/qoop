package io.qoop.unifier.response.filter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test")
class TestController {

    @GetMapping("/ok")
    public Map<String, String> ok() {
        return Map.of("name", "davood");
    }

    @GetMapping("/error")
    public ResponseEntity<Map<String, String>> error() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "bad request"));
    }
}
