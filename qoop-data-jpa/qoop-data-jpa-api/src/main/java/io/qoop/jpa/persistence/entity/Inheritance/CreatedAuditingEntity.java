package io.qoop.jpa.persistence.entity.Inheritance;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.slf4j.MDC;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static io.qoop.logs.LogKeys.MDC_KEY;


@Data
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CreatedAuditingEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    // Correlation column (Correlation ID)
    @Column(name = "correlation_id", length = 50, updatable = false)
    private String correlationId;

    @PrePersist
    public void setCorrelationIdOnPersist() {
        // Reads the value from MDC (which was set in the filter)
        this.correlationId = MDC.get(MDC_KEY);
    }
}
