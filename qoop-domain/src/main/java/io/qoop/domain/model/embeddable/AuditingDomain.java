package io.qoop.domain.model.embeddable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AuditingDomain<ID> extends IDDomain<ID> {
    private AuditInfo audit = new AuditInfo();
}