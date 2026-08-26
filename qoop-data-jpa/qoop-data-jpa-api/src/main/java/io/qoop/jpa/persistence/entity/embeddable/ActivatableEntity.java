package io.qoop.jpa.persistence.entity.embeddable;


import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class ActivatableEntity<ID> extends AuditingEntity<ID> {
    protected boolean activated;
}
