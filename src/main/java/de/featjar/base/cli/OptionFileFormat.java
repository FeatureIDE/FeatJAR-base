package de.featjar.base.cli;

import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.AInputMapper;

public class OptionFileFormat implements IFormat<OptionFile> {
    @Override
    public Result<OptionFile> parse(AInputMapper inputMapper) {
        return Result.of(new OptionFile(inputMapper.get().getInputStream()));
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public boolean supportsSerialize() {
        return false;
    }

    @Override
    public String getFileExtension() {
        return "properties";
    }

    @Override
    public String getName() {
        return "Configuration File";
    }
}
