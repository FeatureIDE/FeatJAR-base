package de.featjar.base.extension;

/**
 * An extension that can be (de-)initialized.
 * Is used for setting up and tearing down global behaviors of FeatJAR,
 * such as {@link de.featjar.base.log.Log} and {@link de.featjar.base.data.Store}.
 *
 * @author Elias Kuiter
 */
public interface Initializer extends Extension {
}
