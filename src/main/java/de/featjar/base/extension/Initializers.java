package de.featjar.base.extension;

/**
 * Extension point for registering static (de-)initializers.
 *
 * @author Elias Kuiter
 */
public class Initializers extends ExtensionPoint<Extension> {
    private static final Initializers INSTANCE = new Initializers();

    public static Initializers getInstance() {
        return INSTANCE;
    }

    private Initializers() {}

    @Override
    public ExtensionPoint<Extension> getInstanceAsExtensionPoint() {
        return INSTANCE;
    }
}
