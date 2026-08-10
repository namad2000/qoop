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
public class AuditInfo {

    @Builder.Default
    private CreationAudit creation = new CreationAudit();

    @Builder.Default
    private UpdateAudit update = new UpdateAudit();
}