package io.qoop.jpa.persistence.entity.embeddable;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class IDEntity<ID> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ENTITY_SEQ")
    @Column(name = "ID", nullable = false)
    private ID id;
}