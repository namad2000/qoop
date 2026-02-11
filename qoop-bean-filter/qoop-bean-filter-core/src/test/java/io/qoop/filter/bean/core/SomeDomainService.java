package ir.online.commons.filter.bean.core;


import io.qoop.filter.bean.api.DomainService;

// Example domain service annotated with your custom annotation
@DomainService
public class SomeDomainService {

    public String process(String input) {
        // Simple service logic
        return "Processed: " + input;
    }
}
