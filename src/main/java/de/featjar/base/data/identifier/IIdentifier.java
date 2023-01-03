package de.featjar.base.data.identifier;

/**
 * Uniquely identifies an {@link IIdentifiable} object.
 * Implementors are responsible for guaranteeing uniqueness within a self-chosen scope (e.g., during program execution).
 *
 * @author Elias Kuiter
 */
public interface IIdentifier {
    IIdentifierFactory getFactory();

    IIdentifier getNewIdentifier();
}
