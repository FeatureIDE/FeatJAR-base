package org.spldev.util.extension;

/**
 * An extension containing a unique ID and a method for initialization
 *
 * @author Sebastian Krieter
 */
public interface Extension {

	/**
	 * @return the unique ID of this extension.
	 */
	String getId();

	/**
	 * Is called, when the extension is loaded for the first time by an
	 * {@link ExtensionPoint}.
	 *
	 * @return {@code true} if the initialization was successful, {@code false}
	 *         otherwise.
	 */
	default boolean initExtension() {
		return true;
	}

}
