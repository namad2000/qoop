package io.qoop.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/7/2025 4:59 PM
 * Package: ir.online.commons.infrastructure.model
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IDModel<ID> {

    protected ID id;
}
