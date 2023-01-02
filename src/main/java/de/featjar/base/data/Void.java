package de.featjar.base.data;

/**
 * A non-null unit value.
 * Can be used to distinguish an erroneous empty result (i.e., {@link Result#empty(Problem...)})
 * from an intended empty result (i.e., {@link Result#ofVoid(Problem...)}).
 * This is useful to return a {@link Result} from a method that should return {@code void}.
 * This is necessary because Java only has the null unit value {@link java.lang.Void}.
 */
public class Void extends Result<java.lang.Void> {
    protected static final de.featjar.base.data.Void VOID = new de.featjar.base.data.Void();

    protected Void() {
        super(null, null);
    }
}
