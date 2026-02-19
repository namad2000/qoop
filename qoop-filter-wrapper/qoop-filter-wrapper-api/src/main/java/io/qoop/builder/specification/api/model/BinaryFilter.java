package io.qoop.builder.specification.api.model;


import lombok.*;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class BinaryFilter {
    private Filter first;
    private Filter second;
}