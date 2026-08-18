package io.qoop.security.resource.user;

import io.qoop.security.api.CurrentUser;
import io.qoop.security.api.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 1/4/2026 4:37 PM
 * Package: io.qoop.security.resource
 */

@RestController
@RequestMapping("/api/test")
public class TestUserController {

    @GetMapping("/me")
    public String getMyName(@CurrentUser User user) {
        // 'user' is automatically populated by the Resolver
        return "Current user is: " + user.getName();
    }

    public void unannotatedMethod(User user) {
    }

    public String getString(@CurrentUser String value) {
        return value;
    }
}
