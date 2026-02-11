package io.qoop.security.resource;


import io.qoop.security.api.PrefixPath;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
class TestController {

    @GetMapping(PrefixPath.INTERNAL + "/hello")
    public String publicHello() {
        return "public";
    }

    @GetMapping("/private/hello")
    public String privateHello() {
        return "private";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/hello")
    public String adminHello() {
        return "admin";
    }

    // New endpoint for testing the whitelist
    @GetMapping("/public/api")
    public String whitelistedApi() {
        return "whitelisted content";
    }
}

