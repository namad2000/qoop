package io.qoop.unifier.response.filter;

import io.qoop.unifier.response.model.StandardResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import static io.qoop.logs.LogKeys.MDC_KEY;

@ControllerAdvice
public class BodyRewrite implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // Apply to all responses
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        HttpServletResponse servletResponse =
                ((ServletServerHttpResponse) response).getServletResponse();

        int statusCode = servletResponse.getStatus();
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.OK;
        }

        if (HttpStatus.OK.equals(status)) {
            if (!MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)) {
                return body;
            }

            String correlationId = MDC.get(MDC_KEY);
            String message = status.isError() ? status.getReasonPhrase() : null;

            return StandardResponse.builder()
                    .code(statusCode)
                    .message(message)
                    .correlationId(correlationId) // اضافه کردن به response
                    .data(statusCode < 500 ? body : null)
                    .build();
        }

        return body;
    }
}
