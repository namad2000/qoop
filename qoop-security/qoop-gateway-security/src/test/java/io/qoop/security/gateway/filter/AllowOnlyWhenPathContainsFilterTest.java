package io.qoop.security.gateway.filter;

import io.qoop.security.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@ExtendWith(MockitoExtension.class)
class AllowOnlyWhenPathContainsFilterTest {

    @Mock
    private SecurityProperties securityProps;

    @Mock
    private SecurityProperties.PathContain pathContainConfig;

    @InjectMocks
    private AllowOnlyWhenPathContainsFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = org.mockito.Mockito.mock(FilterChain.class);

        // Setup default behavior for nested mocks
        when(securityProps.getPathContain()).thenReturn(pathContainConfig);
    }

    @Test
    void doFilterInternal_whenPathContainsKeyword_shouldProceed() throws ServletException, IOException {
        // Arrange: Set the keyword and a path containing it
        String keyword = "secure";
        when(pathContainConfig.getKeyword()).thenReturn(keyword);
        request.setRequestURI("/api/secure/data");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert: Verify the chain continued and status is OK
        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_whenPathIsWhitelisted_shouldProceed() throws ServletException, IOException {
        // Arrange: Set a keyword (not present in path) but whitelist the path pattern
        String keyword = "secure";
        when(pathContainConfig.getKeyword()).thenReturn(keyword);
        when(securityProps.getWhitelistUrls()).thenReturn(List.of("/public/**"));

        request.setRequestURI("/public/health");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert: Verify the chain continued because it is whitelisted
        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_whenPathNotInWhitelistAndNoKeyword_shouldReject() throws ServletException, IOException {
        // Arrange: Set keyword and reject status, path has neither
        String keyword = "secure";
        int rejectStatus = 403; // Forbidden
        when(pathContainConfig.getKeyword()).thenReturn(keyword);
        when(pathContainConfig.getRejectStatus()).thenReturn(rejectStatus);
        when(securityProps.getWhitelistUrls()).thenReturn(List.of("/public/**"));

        request.setRequestURI("/api/random/data");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert: Verify the chain was NOT called and status is Reject Status
        verify(filterChain, org.mockito.Mockito.never()).doFilter(request, response);
        assertEquals(rejectStatus, response.getStatus());
    }
}
