package io.qoop.domain.model.embeddable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SoftDeletionAudit {
    private Boolean deleted = Boolean.FALSE;
    private LocalDateTime deletedAt;
    private String deletedBy;
}