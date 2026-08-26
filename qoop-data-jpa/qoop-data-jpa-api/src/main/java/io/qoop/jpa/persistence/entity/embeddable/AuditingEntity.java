package io.qoop.jpa.persistence.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@MappedSuperclass
public abstract class AuditingEntity<ID> extends IDEntity<ID> {

    @Embedded
    protected AuditInfo audit = new AuditInfo();

    @Version
    @Column(name = "VERSION", nullable = false)
    protected Long version = 0L;
}