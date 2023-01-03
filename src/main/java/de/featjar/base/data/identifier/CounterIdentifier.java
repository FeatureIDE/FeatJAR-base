package de.featjar.base.data.identifier;

/**
 * Identifies an object with a given number.
 *
 * @author Elias Kuiter
 */
public class CounterIdentifier extends AIdentifier {
    protected final long counter;

    public CounterIdentifier(long counter, Factory factory) {
        super(factory);
        this.counter = counter;
    }

    public long getCounter() {
        return counter;
    }

    @Override
    public String toString() {
        return String.valueOf(counter);
    }

    /**
     * Creates counter identifiers by incrementing a number.
     */
    public static class Factory implements IIdentifierFactory {
        long counter = 0;

        @Override
        public IIdentifier get() {
            return new CounterIdentifier(++counter, this);
        }

        @Override
        public IIdentifier parse(String identifierString) {
            return new CounterIdentifier(Long.parseLong(identifierString), this);
        }
    }
}
