package io.qoop.fault.handler.rest;

import io.qoop.fault.handler.api.exception.DomainBusinessException;
import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.fault.handler.api.exception.DomainValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/test")
public class TestController {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SampleRequestDto {
        @NotBlank(message = "Name cannot be blank")
        private String name;

        @Min(value = 18, message = "Age must be at least 18")
        private int age;
    }

    @PostMapping("/dto-validation")
    public void validateDto(@Valid @RequestBody SampleRequestDto request) {
    }

    @GetMapping("/param-validation")
    public void validateParam(@RequestParam @Min(value = 10, message = "Value must be at least 10") int count) {
    }

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