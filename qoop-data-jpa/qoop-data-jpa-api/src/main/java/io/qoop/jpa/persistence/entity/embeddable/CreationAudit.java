package io.qoop.jpa.persistence.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Embeddable
public class CreationAudit {

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "CREATED_BY", nullable = false, updatable = false, length = 10)
    protected String createdBy;
}