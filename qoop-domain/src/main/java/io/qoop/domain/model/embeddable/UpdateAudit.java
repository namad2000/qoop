package io.qoop.domain.model.embeddable;


import lombok.Builder;
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
    private LocalDateTime updatedAt;
    private String updatedBy;

    @Builder.Default
    private Long version = 0L;
}