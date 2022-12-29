package de.featjar.base.cli;

import de.featjar.base.data.Result;

/**
 * A string option, which is parsed as itself.
 */
public class StringOption extends Option<String> {
    /**
     * Creates a string option.
     *
     * @param name the name of the string option
     */
    public StringOption(String name) {
        super(name, Result.mapReturnValue(String::valueOf));
    }
}
