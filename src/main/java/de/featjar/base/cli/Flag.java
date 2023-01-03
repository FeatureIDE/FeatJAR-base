package de.featjar.base.cli;

import de.featjar.base.data.Result;

/**
 * A Boolean flag option, which can either be present or not.
 *
 * @author Elias Kuiter
 */
public class Flag extends Option<Boolean> {
    /**
     * Creates a flag option.
     *
     * @param name the name of the flag option
     */
    public Flag(String name) {
        super(name, null);
    }

    @Override
    public Result<Boolean> parseFrom(AArgumentParser argumentParser) throws AArgumentParser.ArgumentParseException {
        boolean value = argumentParser.parseFlag(name);
        if (defaultValue != null || isRequired)
            throw new IllegalArgumentException();
        return Result.of(value);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", name, description);
    }
}
