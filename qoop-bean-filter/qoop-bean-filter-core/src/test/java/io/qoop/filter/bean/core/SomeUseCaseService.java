package io.qoop.filter.bean.core;


import io.qoop.filter.bean.api.UseCaseService;

// Example use-case service annotated with your custom annotation
@UseCaseService
public class SomeUseCaseService {

    public String execute(String param) {
        // Simple use-case logic
        return "Executed: " + param;
    }
}
