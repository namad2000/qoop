package io.qoop.jpa.persistence.entity.embeddable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class AuditInfo {

    @Embedded
    private CreationAudit creation = new CreationAudit();

    @Embedded
    private UpdateAudit update = new UpdateAudit();
}