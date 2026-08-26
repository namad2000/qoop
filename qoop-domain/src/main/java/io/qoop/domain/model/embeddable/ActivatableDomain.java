package io.qoop.domain.model.embeddable;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ActivatableDomain<ID> extends AuditingDomain<ID> {
    protected boolean activated;
}
