package de.featjar.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ProcessOutput {

    public static ProcessOutput runProcess(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);

        InputStream processErr = process.getErrorStream();
        InputStream processOut = process.getInputStream();
        BufferedReader outbr = new BufferedReader(new InputStreamReader(processOut, StandardCharsets.UTF_8));
        BufferedReader errbr = new BufferedReader(new InputStreamReader(processErr, StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = outbr.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        String outputString = sb.toString();

        sb = new StringBuilder();
        line = null;
        while ((line = errbr.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        String errorString = sb.toString();

        return new ProcessOutput(outputString, errorString);
    }

    private final String outputString;
    private final String errorString;

    public ProcessOutput(String outputString, String errorString) {
        this.outputString = outputString;
        this.errorString = errorString;
    }

    public void printOutput() {
        System.out.println(outputString);
        if (!errorString.isBlank()) {
            System.err.println(errorString);
        }
    }

    public String getOutputString() {
        return outputString;
    }

    public String getErrorString() {
        return errorString;
    }
}
