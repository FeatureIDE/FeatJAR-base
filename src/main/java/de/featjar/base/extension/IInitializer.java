package de.featjar.base.extension;

import de.featjar.base.computation.Cache;

/**
 * An extension that can be (de-)initialized.
 * Is used for setting up and tearing down global behaviors of FeatJAR,
 * such as {@link de.featjar.base.log.Log} and {@link Cache}.
 *
 * @author Elias Kuiter
 */
public interface IInitializer extends IExtension {}
