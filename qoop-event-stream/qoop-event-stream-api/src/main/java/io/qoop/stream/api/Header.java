package io.qoop.stream.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 9/2/2026 6:02 PM
 * Package: io.qoop.stream.api
 */

@Setter
@Getter
@AllArgsConstructor
public class Header {
    private String name;
    private String value;
}
