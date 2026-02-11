package io.qoop.application.model.cmd;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/7/2025 4:59 PM
 * Package: io.qoop.infrastructure.model
 */

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActiveCmd<ID> extends UpdateCmd<ID> {

    @Builder.Default
    protected Boolean isActive = true;
}
