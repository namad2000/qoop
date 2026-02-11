package io.qoop.security.gateway.filter;

import io.qoop.security.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(-200)
public class AllowOnlyWhenPathContainsFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProps;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AllowOnlyWhenPathContainsFilter(SecurityProperties securityProps) {
        this.securityProps = securityProps;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String keyword = securityProps.getPathContain().getKeyword();
        int status = securityProps.getPathContain().getRejectStatus();

        boolean isAllowed = path.contains(keyword) || isWhitelisted(securityProps.getWhitelistUrls(), path);

        if (!isAllowed) {
            response.setStatus(status);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(List<String> whitelist, String path) {
        if (whitelist == null) {
            return false;
        }
        return whitelist.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
