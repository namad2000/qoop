package io.qoop.jpa.persistence.specification;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TEST_ENTITY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer age;
    private Boolean active;
}