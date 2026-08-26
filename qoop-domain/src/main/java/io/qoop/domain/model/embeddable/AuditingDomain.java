package io.qoop.domain.model.embeddable;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class AuditingDomain<ID> extends IDDomain<ID> {

    @Builder.Default
    protected AuditInfo audit = new AuditInfo();

    @Builder.Default
    protected Long version = 0L;
}