package org.sk.utils.io;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Writer for CSV files.
 * <p>
 * After instantiation set the path to the output file with
 * {@link #setOutputDirectory(Path)} and subsequently {@link #setFileName}. Then
 * change the default values of the following properties, if necessary:
 * <ul>
 * <li>{@link #setKeepLines(boolean)}: If {@code true}, keeps the data stored
 * within the CSV Writer even after they are written to the output file.
 * (Default: {@code false})
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

	private static final String NEWLINE = System.lineSeparator();
	private static final String DEFAULT_SEPARATOR = ";";

	private final List<List<String>> values = new ArrayList<>();

	private String separator = DEFAULT_SEPARATOR;
	private List<String> header = null;

	private Path outputDirectoryPath = Paths.get("");
	private Path outputFilePath;

	private boolean keepLines = false;
	private boolean append = false;

	private boolean newFile = true;
	private int nextLine = 0;

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

	public void setFileName(Path fileName) throws IOException {
		setOutputFile(outputDirectoryPath.resolve(fileName));
	}

	public void setFileName(String fileName) throws IOException {
		setOutputFile(outputDirectoryPath.resolve(fileName));
	}

	private void setOutputFile(Path outputFile) throws IOException {
		outputFilePath = outputFile;
		if (append) {
			newFile = !Files.exists(outputFile);
		} else {
			Files.deleteIfExists(outputFile);
			newFile = true;
		}
		if (newFile) {
			Files.createFile(outputFile);
		}
		reset();
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public List<String> getHeader() {
		return header;
	}

	public void setHeader(List<String> header) {
		this.header = new ArrayList<>(header);
		if (values.isEmpty()) {
			values.add(this.header);
		} else {
			values.set(0, this.header);
		}
		if (!newFile) {
			nextLine = 1;
		}
	}

	public void addHeaderValue(String headerValue) {
		header.add(headerValue);
	}

	public void addLine(List<String> line) {
		values.add(line);
	}

	public void createNewLine() {
		values.add(new ArrayList<String>());
	}

	public void flush() {
		if (outputFilePath != null) {
			final StringBuilder sb = new StringBuilder();
			for (int i = nextLine; i < values.size(); i++) {
				writer(sb, values.get(i));
			}
			try {
				Files.write(outputFilePath, sb.toString().getBytes(), StandardOpenOption.APPEND);
				if (keepLines) {
					nextLine = values.size();
				} else {
					values.subList(1, values.size()).clear();
					nextLine = 1;
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addValue(Object o) {
		values.get(values.size() - 1).add(o.toString());
	}

	public List<List<String>> getValues() {
		return values;
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

	public boolean saveToFile(Path p) {
		try {
			Files.write(p, toString().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void reset() {
		if (!values.isEmpty()) {
			values.subList(1, values.size()).clear();
		}
		nextLine = 0;
	}

	public void resetLine() {
		if (!values.isEmpty()) {
			values.remove(values.size() - 1);
		}
	}

	public boolean isKeepLines() {
		return keepLines;
	}

	public void setKeepLines(boolean keepLines) {
		this.keepLines = keepLines;
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
		final StringBuilder sb = new StringBuilder();
		for (final List<String> line : values) {
			writer(sb, line);
		}
		return sb.toString();
	}

}
