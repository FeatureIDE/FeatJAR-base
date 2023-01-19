package de.featjar.base.cli;

import de.featjar.base.data.Result;
import de.featjar.base.data.Void;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Parses options from a file.
 *
 * @author Elias Kuiter
 */
public class OptionFile implements IOptionInput {
    Properties properties;
    Properties unusedProperties;

    /**
     * Creates an option file.
     *
     * @param inputStream the input stream
     */
    public OptionFile(InputStream inputStream) {
        this.properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.unusedProperties = (Properties) properties.clone();
    }

    /**
     * {@return the properties of this option file}
     */
    public Properties getProperties() {
        return properties;
    }

    @Override
    public Result<Void> validate(List<Option<?>> options) {
        options.forEach(this::get);
        return unusedProperties.size() == 0 ? Result.ofVoid() : Result.empty();
    }

    @Override
    public <T> Result<T> get(Option<T> option) {
        unusedProperties.remove(option.getName());
        return option.parseFrom(this);
    }
}
