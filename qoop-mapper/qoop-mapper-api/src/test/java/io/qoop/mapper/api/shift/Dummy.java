package io.qoop.mapper.api.shift;

public class Dummy {
    public String name;

    public Dummy() {
    }

    public Dummy(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "{\"name\":\"" + name + "\"}";
    }
}
