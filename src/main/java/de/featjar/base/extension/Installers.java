package de.featjar.base.extension;

/**
 * Extension point for registering static (de-)initializers.
 *
 * @author Elias Kuiter
 */
public class Installers extends ExtensionPoint<Extension> {
    private static final Installers INSTANCE = new Installers();

    public static Installers getInstance() {
        return INSTANCE;
    }

    private Installers() {}

    @Override
    public ExtensionPoint<Extension> getInstanceAsExtensionPoint() {
        return INSTANCE;
    }
}
