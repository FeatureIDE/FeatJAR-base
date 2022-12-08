/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of util.
 *
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/util> for further information.
 */
package de.featjar.util.io.csv;

import de.featjar.util.logging.Logger;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Writer for CSV files.
 * <p>
 * After instantiation set the path to the output file with
 * {@link #setOutputDirectory(Path)} and subsequently {@link #setFileName}. Then
 * change the default values of the following properties, if necessary:
 * <ul>
 * <li>{@link #setAppend(boolean)}: If {@code true}, writes data at the end of
 * the output file. If {@code false}, overwrites the output file. (Default:
 * {@code false})
 * <li>{@link #setSeparator(String)}: Sets the value separator in the output
 * file. (Default: {@code ';'})
 * <li>{@link #setHeader(List)}: Sets a header in the output file. (Default:
 * empty)
 * </ul>
 * Add data with {@link #addValue(Object)} to and write everything to the output
 * file with {@link #flush()}.
 *
 * @author Sebastian Krieter
 */
public class CSVWriter {

    private static final DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    static {
        df.setMaximumFractionDigits(340);
    }

    private static final String NEWLINE = System.lineSeparator();
    private static final String DEFAULT_SEPARATOR = ";";

    private final LinkedList<List<String>> values = new LinkedList<>();

    private String separator = DEFAULT_SEPARATOR;
    private List<String> header = null;

    private Path outputDirectoryPath = Paths.get("");
    private Path outputFilePath;
    private Consumer<CSVWriter> lineWriter;

    private boolean append = false;

    private boolean newFile = true;

    public Path getOutputDirectory() {
        return outputDirectoryPath;
    }

    public boolean setOutputDirectory(Path outputPath) throws IOException {
        if (Files.isDirectory(outputPath)) {
            outputDirectoryPath = outputPath;
            return true;
        } else if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            outputDirectoryPath = outputPath;
            return true;
        } else {
            return false;
        }
    }

    public void setFileName(String fileName) throws IOException {
        setOutputFile(outputDirectoryPath.resolve(fileName));
    }

    public void setOutputFile(Path outputFile) throws IOException {
        setOutputDirectory(outputFile.toAbsolutePath().getParent());
        outputFilePath = outputFile;
        newFile = true;
        reset();
    }

    public void setLineWriter(Consumer<CSVWriter> lineWriter) {
        this.lineWriter = lineWriter;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public List<String> getHeader() {
        return Collections.unmodifiableList(header);
    }

    public void setHeader(String... header) {
        setHeader(Arrays.asList(header));
    }

    public void setHeader(List<String> header) {
        this.header = new ArrayList<>(header);
    }

    public void addHeaderValue(String headerValue) {
        header.add(headerValue);
    }

    public void addLine(List<String> line) {
        values.add(line);
    }

    public void createNewLine() {
        values.add(header != null ? new ArrayList<>(header.size()) : new ArrayList<>());
    }

    public void addValue(Object value) {
        values.get(values.size() - 1).add(String.valueOf(value));
    }

    /**
     * Formats float values manually to avoid scientific notation and non-English
     * punctuation.
     */
    public void addValue(float value) {
        addValue(df.format(value));
    }

    public void addValue(double value) {
        addValue(df.format(value));
    }

    public void addValue(Float value) {
        addValue(value);
    }

    public void addValue(Double value) {
        addValue(value);
    }

    public void addValue(BigDecimal value) {
        addValue(value.doubleValue());
    }

    public final void writeLine() {
        createNewLine();
        try {
            lineWriter.accept(this);
        } catch (final Exception e) {
            removeLastLine();
            throw e;
        }
        flush();
    }

    public void flush() {
        if (outputFilePath != null) {
            try {
                final StringBuilder sb = new StringBuilder();
                if (newFile && (header != null)) {
                    if (!append || !Files.exists(outputFilePath) || (Files.size(outputFilePath) == 0)) {
                        writer(sb, header);
                    }
                }
                values.stream().forEach(line -> writer(sb, line));
                final byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
                if (newFile && !append) {
                    Files.write(
                            outputFilePath,
                            bytes,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                    newFile = false;
                } else {
                    Files.write(
                            outputFilePath,
                            bytes,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND);
                }
                values.clear();
            } catch (final IOException e) {
                Logger.logError(e);
            }
        }
    }

    private void writer(StringBuilder sb, List<String> line) {
        for (final String value : line) {
            if (value != null) {
                sb.append(value);
            }
            sb.append(separator);
        }
        if (line.isEmpty()) {
            sb.append(NEWLINE);
        } else {
            final int length = sb.length() - 1;
            sb.replace(length, length + separator.length(), NEWLINE);
        }
    }

    public void reset() {
        values.clear();
    }

    public void removeLastLine() {
        if (!values.isEmpty()) {
            values.remove(values.size() - 1);
        }
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    @Override
    public int hashCode() {
        return outputFilePath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        return Objects.equals(outputFilePath, ((CSVWriter) obj).outputFilePath);
    }

    @Override
    public String toString() {
        return "CSVWriter [path = " + outputFilePath + ", header = " + header + "]";
    }
}
