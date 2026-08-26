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
public class UpdateAudit {
    protected LocalDateTime updatedAt;
    protected String updatedBy;
}