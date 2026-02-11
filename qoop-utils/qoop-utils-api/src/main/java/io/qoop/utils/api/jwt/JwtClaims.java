package io.qoop.utils.api.jwt;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@Builder
public class JwtClaims {
    private String subject;
    private Date expiration;
    private Date issuedAt;
    private Map<String, Object> additionalInfo;
}