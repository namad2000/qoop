package io.qoop.application.model.cmd;

import io.qoop.validation.api.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/7/2025 4:59 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateCmd<ID> {

    @NotEmpty
    protected ID id;

    @NotEmpty
    protected Integer version;
}
