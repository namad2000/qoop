package io.qoop.domain.model.embeddable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AuditInfo {
    private CreationAudit creation = new CreationAudit();
    private UpdateAudit update = new UpdateAudit();
}