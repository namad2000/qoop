package io.qoop.domain.model.Inheritance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/7/2025 4:59 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreatedModel<ID> {
    private LocalDateTime createdAt;
    private String createdBy;
}
