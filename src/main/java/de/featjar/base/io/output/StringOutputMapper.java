package de.featjar.base.io.output;

import de.featjar.base.data.Maps;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps virtual paths to string outputs.
 */
public class StringOutputMapper extends AOutputMapper {
    protected final Charset charset;

    /**
     * Creates a file output mapper for a collection of strings.
     *
     * @param charset the charset
     */
    public StringOutputMapper(Charset charset) {
        super(Maps.of(DEFAULT_MAIN_PATH, new StringOutput(charset)), DEFAULT_MAIN_PATH);
        this.charset = charset;
    }

    @Override
    protected AOutput newOutput(Path path) {
        return new StringOutput(charset);
    }

    /**
     * {@return the collection of strings}
     */
    public LinkedHashMap<Path, java.lang.String> getOutputStrings() {
        return ioMap.entrySet().stream()
                .collect(Maps.toMap(
                        Map.Entry::getKey, e -> e.getValue().getOutputStream().toString()));
    }
}
