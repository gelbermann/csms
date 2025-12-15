package com.cp.csms.transactions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DriverIdentifier {

    private final String id;

    @JsonCreator
    public DriverIdentifier(@JsonProperty("id") String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "DriverIdentifier{" +
                "id='" + id + '\'' +
                '}';
    }
}