package org.sk.utils.extension;

/**
 * A FeatureIDE extension with its ID.
 * <p>
 * <b>NOTE:</b> All extensions should provide a default/nullary constructor.
 *
 * @author Sebastian Krieter
 */
public interface Extension {

	/**
	 * @return the unique ID of this extension.
	 */
	String getId();

	/**
	 * Is called, when the extension is loaded for the first time by the
	 * {@link ExtensionManager}.
	 *
	 * @return {@code true} if the initialization was successful, {@code false}
	 *         otherwise.
	 */
	default boolean initExtension() {
		return true;
	}

}
