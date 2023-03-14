package de.featjar.base;

/**
 * Utilities for handling exceptions.
 *
 * @author Elias Kuiter
 */
public class Exceptions {
    /**
     * {@return whether the given throwable has been caused by the given class}
     *
     * @param throwable the throwable
     * @param klass the class
     */
    public static boolean isCausedBy(Throwable throwable, Class<? extends Throwable> klass) {
        if (throwable.getCause() == null)
            return false;
        else if (throwable.getCause().getClass().equals(klass))
            return true;
        else
            return isCausedBy(throwable.getCause(), klass);
    }
}
