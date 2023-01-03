package de.featjar.base.data.identifier;

public class Identifiers {
    public static IIdentifier newCounterIdentifier() {
        return new CounterIdentifier.Factory().get();
    }

    public static IIdentifier newUUIDIdentifier() {
        return new UUIDIdentifier.Factory().get();
    }
}
