package io.qoop.fault.handler.rest;

import io.qoop.fault.handler.api.exception.DomainBusinessException;
import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.fault.handler.api.exception.DomainValidationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/validation-exception")
    public void throwValidation() {
        throw DomainValidationException.withParams(
                "validation.required",
                "email",
                "email"
        );
    }

    @GetMapping("/domain-exception")
    public void throwDomain() {
        throw DomainException.of(
                "business.rule.failed",
                409,
                "Some parameter"
        );
    }

    @GetMapping("/business-exception")
    public void throwBusiness() {
        throw DomainBusinessException.withParams(
                "business.logic.error",
                "AccountBalance"
        );
    }

    @GetMapping("/generic-exception")
    public void throwGeneric() {
        throw new RuntimeException("Unexpected system error");
    }

    @GetMapping("/runtime-exception")
    public void throwRuntime() {
        throw new RuntimeException("Original Exception Message");
    }
}

