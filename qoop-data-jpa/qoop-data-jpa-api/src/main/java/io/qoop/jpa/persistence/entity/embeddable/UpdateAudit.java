package io.qoop.jpa.persistence.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Embeddable
public class UpdateAudit {

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "UPDATED_BY", length = 10)
    private String updatedBy;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version = 0L;
}