package io.qoop.unifier.response.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/ok")
    public Map<String, String> ok() {
        return Map.of("name", "davood");
    }

    @GetMapping("/string")
    public String stringResponse() {
        return "plain string data";
    }

    @GetMapping("/error")
    public ResponseEntity<Map<String, String>> error() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "bad request"));
    }

    @GetMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> image() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body("fake-image-bytes".getBytes());
    }

    @GetMapping(value = "/plain-text", produces = MediaType.TEXT_PLAIN_VALUE)
    public String plainText() {
        return "raw text content";
    }
}
