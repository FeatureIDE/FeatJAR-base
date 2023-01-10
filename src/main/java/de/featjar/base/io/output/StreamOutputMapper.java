package de.featjar.base.io.output;

import de.featjar.base.data.Maps;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * Maps virtual paths to stream outputs.
 */
public class StreamOutputMapper extends AOutputMapper {
    protected StreamOutputMapper(LinkedHashMap<Path, AOutput> ioMap, Path mainPath) {
        super(ioMap, mainPath);
    }

    /**
     * Creates a stream output mapper for a single stream.
     *
     * @param outputStream the output stream
     * @param charset      the charset
     */
    public StreamOutputMapper(OutputStream outputStream, Charset charset) {
        super(Maps.of(DEFAULT_MAIN_PATH, new StreamOutput(outputStream, charset)), DEFAULT_MAIN_PATH);
    }

    @Override
    protected AOutput newOutput(Path path) {
        throw new UnsupportedOperationException("cannot guess kind of requested output stream");
    }
}
