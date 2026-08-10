package io.qoop.jpa.persistence.entity.embeddable;

import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@MappedSuperclass
public abstract class AuditingEntity<ID> extends IDEntity<ID> {

    @Embedded
    private AuditInfo audit = new AuditInfo();
}