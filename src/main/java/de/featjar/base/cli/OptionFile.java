package de.featjar.base.cli;

import de.featjar.base.data.Result;
import de.featjar.base.data.Void;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class OptionFile implements IOptionInput {
    Properties properties;
    Properties unusedProperties;

    public OptionFile(InputStream inputStream) {
        this.properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.unusedProperties = (Properties) properties.clone();
    }

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
