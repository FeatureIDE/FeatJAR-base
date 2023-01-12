package de.featjar.base.data;

/**
 * Use this interface to signal that a class overrides {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * Implementation cannot be enforced in Java, so be careful to only use this interface when custom implementations exist.
 * This interface can then be used in contexts where objects must be hashable (e.g., in a {@link java.util.LinkedHashMap}).
 */
public interface IHashable {
    boolean equals(Object object);
    int hashCode();
}
