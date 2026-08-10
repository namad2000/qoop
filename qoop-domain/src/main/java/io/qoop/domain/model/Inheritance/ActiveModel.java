package io.qoop.domain.model.Inheritance;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/7/2025 4:59 PM
 */

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActiveModel<ID> extends UpdateModel<ID> {

    @Builder.Default
    protected boolean active = true;
}
