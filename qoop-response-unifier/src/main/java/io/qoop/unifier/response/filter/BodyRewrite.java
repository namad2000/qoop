package io.qoop.unifier.response.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.unifier.response.model.StandardResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Date;
import java.util.List;

import static io.qoop.logs.LogKeys.MDC_KEY;

@RestControllerAdvice
@RequiredArgsConstructor
public class BodyRewrite implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Value("${qoop.unifier.swagger-paths:/v3/api-docs,/swagger-ui,/swagger-resources}")
    private List<String> swaggerPaths;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {

        // 1. Skip if already wrapped
        if (returnType.getParameterType().equals(StandardResponse.class)) {
            return false;
        }

        // 2. Skip if the return type belongs to OpenAPI / Swagger internal models
        Class<?> declaringClass = returnType.getDeclaringClass();
        if (declaringClass.getName().startsWith("org.springdoc") ||
                declaringClass.getName().startsWith("io.swagger")) {
            return false;
        }

        // 3. Process ONLY JSON and String converters
        return JacksonJsonHttpMessageConverter.class.isAssignableFrom(converterType)
                || StringHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body == null) {
            return null;
        }

        if (!MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)) {
            return body;
        }

        // Check configurable Swagger paths
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            String requestURI = httpServletRequest.getRequestURI();
            if (isSwaggerPath(requestURI)) {
                return body;
            }
        }

        if (body instanceof StandardResponse) {
            return body;
        }

        HttpServletResponse servletResponse =
                ((ServletServerHttpResponse) response).getServletResponse();

        int statusCode = servletResponse.getStatus();
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.OK;
        }

        if (status.isError()) {
            return body;
        }

        String correlationId = MDC.get(MDC_KEY);

        StandardResponse<Object> standardResponse = StandardResponse.builder()
                .code(status.getReasonPhrase())
                .correlationId(correlationId)
                .timestamp(new Date())
                .data(body)
                .build();

        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType) || body instanceof String) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return objectMapper.writeValueAsString(standardResponse);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting StandardResponse to JSON String", e);
            }
        }

        return standardResponse;
    }

    private boolean isSwaggerPath(String uri) {
        return swaggerPaths.stream().anyMatch(uri::contains);
    }
}