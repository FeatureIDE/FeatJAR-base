package de.featjar.base.data.identifier;

/**
 * Identifies an object with a given {@link UUIDIdentifier}.
 *
 * @author Elias Kuiter
 */
public class UUIDIdentifier extends AIdentifier {
    protected final java.util.UUID uuid;

    public UUIDIdentifier(java.util.UUID uuid, Factory factory) {
        super(factory);
        this.uuid = uuid;
    }

    public java.util.UUID getUUID() {
        return uuid;
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    /**
     * Creates random {@link UUIDIdentifier} identifiers.
     */
    public static class Factory implements IIdentifierFactory {

        @Override
        public AIdentifier get() {
            return new UUIDIdentifier(java.util.UUID.randomUUID(), this);
        }

        @Override
        public AIdentifier parse(String identifierString) {
            return new UUIDIdentifier(java.util.UUID.fromString(identifierString), this);
        }
    }
}
