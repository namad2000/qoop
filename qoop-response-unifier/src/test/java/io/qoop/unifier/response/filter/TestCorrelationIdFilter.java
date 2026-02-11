package io.qoop.unifier.response.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static io.qoop.logs.LogKeys.MDC_KEY;

@Component
class TestCorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        MDC.put(MDC_KEY, "test-correlation-id");
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
