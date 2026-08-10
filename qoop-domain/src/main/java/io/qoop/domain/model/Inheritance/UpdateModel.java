package io.qoop.domain.model.Inheritance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/7/2025 4:59 PM
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateModel<ID> extends CreatedModel<ID> {
    protected ID id;
    protected LocalDateTime updatedAt;
    protected String updatedBy;
    protected Integer version;
}
