package de.featjar.base.data.identifier;

import de.featjar.base.data.IFactory;

/**
 * Parses a string into an {@link IIdentifier}.
 *
 * @author Elias Kuiter
 */
public interface IIdentifierFactory extends IFactory<IIdentifier> {
    IIdentifier parse(String identifierString);
}
