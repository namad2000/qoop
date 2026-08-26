package io.qoop.jpa.persistence.entity.embeddable;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class ActivatableEntity<ID> extends AuditingEntity<ID> {
    @Column(name = "IS_ACTIVATED", nullable = false)
    protected boolean activated;
}
