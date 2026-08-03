package io.qoop.jpa.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class ActiveAuditingEntity extends AuditingEntity {
    @Column(name = "is_active")
    private boolean activated;
}
