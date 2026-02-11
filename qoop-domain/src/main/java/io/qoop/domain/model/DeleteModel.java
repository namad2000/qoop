package io.qoop.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeleteModel<ID> extends ActiveModel<ID> {

    @Builder.Default
    protected Boolean isDeleted = false;
}
