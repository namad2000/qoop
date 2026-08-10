package io.qoop.jpa.persistence.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Embeddable
public class SoftDeletionAudit {

    @Column(name = "IS_DELETED", nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "DELETED_BY", length = 10)
    private String deletedBy;
}