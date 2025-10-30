/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */
package de.featjar.base.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;

public class Shell {
    private static Shell instance;
    private ShellSession session;
    private final Scanner shellScanner;
    private List<String> history;
    private ListIterator<String> historyIterator;
    private final BufferedReader reader;
    private StringBuilder input;
    String historyCommandLine;
    private int cursorX, cursorY;
    private boolean lastArrowKeyUp;
    private final static String START_OF_TERMINAL_LINE = "$ "; 
    private final static int CURSOR_START_POSITION_LENGTH = START_OF_TERMINAL_LINE.length() + 1;

    private Shell() {
        this.session = new ShellSession();
        this.history = new LinkedList<>();
        this.historyIterator = history.listIterator();

        if (isWindows()) {
            this.shellScanner = new Scanner(System.in);
            this.reader = null;
        } else {
            this.shellScanner = null;
            this.reader = new BufferedReader(new InputStreamReader(System.in));
        }
        // TODO remove the next line !
        session.put("p", Paths.get("../feature-model/testFeatureModels/basic.xml"), Path.class);
    }

    public static Shell getInstance() {
        return (instance == null) ? (instance = new Shell()) : instance;
    }

    public static void main(String[] args) {
        Shell.getInstance().run();
    }

    private void run() {
        FeatJAR.initialize(FeatJAR.shellConfiguration());
        printArt();
        new HelpShellCommand().printCommands();
        while (true) {
            List<String> cmdArg = null;
            try {
                cmdArg = readCommand(START_OF_TERMINAL_LINE)
                        .map(c -> Arrays.stream(c.split("\\s+")).collect(Collectors.toList()))
                        .orElse(Collections.emptyList());
            } catch (CancellationException e) {
                FeatJAR.log().message(e.getMessage());
                exitInputMode();
                System.exit(0);
            }
            if (!cmdArg.isEmpty()) {
                try {
                    Result<IShellCommand> command = parseCommand(cmdArg.get(0));
                    cmdArg.remove(0);
                    if (command.isPresent()) {
                        history.add(command.get().getShortName().get() + " "
                                + cmdArg.stream().map(String::valueOf).collect(Collectors.joining(" ")));
                        command.get().execute(session, cmdArg);
                    }
                } catch (CancellationException e) {
                    FeatJAR.log().message(e.getMessage());
                }
            }
        }
    }

    private Result<IShellCommand> parseCommand(String commandString) {
        ShellCommands shellCommandsExentionsPoint = ShellCommands.getInstance();
        List<IShellCommand> commands = shellCommandsExentionsPoint.getExtensions().stream()
                .filter(command -> command.getShortName()
                        .map(name -> name.toLowerCase().startsWith(commandString))
                        .orElse(Boolean.FALSE))
                .collect(Collectors.toList());

        if (commands.size() > 1) {
            Map<Integer, IShellCommand> ambiguousCommands = new HashMap<Integer, IShellCommand>();
            int i = 1;

            FeatJAR.log()
                    .info(
                            ("Command name '%s' is ambiguous! choose one of the following %d commands (leave blank to abort): \n"),
                            commandString,
                            commands.size());

            for (IShellCommand c : commands) {
                FeatJAR.log()
                        .message(i + "." + c.getShortName().get() + " - "
                                + c.getDescription().get());
                ambiguousCommands.put(i, c);
                i++;
            }

            String choice = readCommand("").orElse("");

            if (choice.isBlank()) {
                return Result.empty();
            }
            int parsedChoice;
            try {
                parsedChoice = Integer.parseInt(choice);
            } catch (NumberFormatException e) {
                return Result.empty(addProblem(Severity.ERROR, String.format("'%s' is no vaild number", choice), e));
            }

            for (Map.Entry<Integer, IShellCommand> entry : ambiguousCommands.entrySet()) {
                if (Objects.equals(entry.getKey(), parsedChoice)) {
                    return Result.of(entry.getValue());
                }
            }
            return Result.empty(addProblem(
                    Severity.ERROR,
                    "Command name '%s' is ambiguous! It matches the following commands: \n%s and wrong number !",
                    commandString,
                    commands.stream().map(IShellCommand::getIdentifier).collect(Collectors.joining("\n"))));
        }

        IShellCommand command = null;
        if (commands.isEmpty()) {
            Result<IShellCommand> matchingExtension = shellCommandsExentionsPoint.getMatchingExtension(commandString);
            if (matchingExtension.isEmpty()) {
                FeatJAR.log()
                        .message(
                                "No such command '" + commandString + "'. \n <help> shows all viable commands");
                return Result.empty(addProblem(Severity.ERROR, "No command matched the name '%s'!", commandString));
            }
            command = matchingExtension.get();
        } else {
            if (commands.get(0).getShortName().get().toLowerCase().matches(commandString)) {
                command = commands.get(0);
                return Result.of(command);
            }
            String choice = readCommand(
                            "Do you mean: " + commands.get(0).getShortName().get() + "? (ENTER) or (a)bort\n")
                    .orElse("");
            if (choice.isEmpty()) {
                command = commands.get(0);
            } else {
                return Result.empty();
            }
        }
        return Result.of(command);
    }

    private Problem addProblem(Severity severity, String message, Object... arguments) {
        return new Problem(String.format(message, arguments), severity);
    }

    private void handleTabulatorAutoComplete() {
        if (input.length() == 0) {
            return;
        }
        List<String> commands = ShellCommands.getInstance().getExtensions().stream()
                .filter(command -> command.getShortName()
                        .map(name -> name.toLowerCase().startsWith(String.valueOf(input)))
                        .orElse(Boolean.FALSE))
                .map(cmd -> cmd.getShortName().get().toLowerCase())
                .collect(Collectors.toList());

        if (commands.isEmpty()) {
            return;
        }

        String prefix = commands.get(0);

        for (int i = 1; i < commands.size(); i++) {
            prefix = calculateSimilarPrefix(prefix, commands.get(i));
        }
        input.setLength(0);
        input = input.append(prefix);
        cursorX = input.length();

        displayCharacters(input.toString());
    }

    private String calculateSimilarPrefix(String oldPrefix, String nextString) {
        int minPrefixLength = Math.min(oldPrefix.length(), nextString.length());
        int i = 0;
        while (i < minPrefixLength && oldPrefix.charAt(i) == nextString.charAt(i)) {
            i++;
        }
        return oldPrefix.substring(0, i);
    }

    private boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * Displays the typed characters in the console.
     * '\r' moves the cursor to the beginning of the line
     * '\u001B[2K' or '\033[2K' erases the entire line
     * '\u001B' (unicode) or '\033' (octal) for ESC work fine here
     * '\u001B[#G' moves cursor to column #
     * see for more documentation: https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797
     * @param typedText the typed characters
     */
    private void displayCharacters(String typedText) {
        FeatJAR.log().noLineBreakMessage("\r");
        FeatJAR.log().noLineBreakMessage("\033[2K");
        FeatJAR.log().noLineBreakMessage("$ " + typedText);
        FeatJAR.log().noLineBreakMessage("\033[" + (cursorX + CURSOR_START_POSITION_LENGTH) + "G");
        // TODO cursor dynamic + " " after $
    }
    
    
    private void resetMousePointer() {
        FeatJAR.log().noLineBreakMessage("\033[" + CURSOR_START_POSITION_LENGTH + "G");
    }

    /*
     * TODO
     */

    public static Optional<String> readCommand(String prompt) {
        return Shell.getInstance().readShellCommand(prompt);
    }

    private Optional<String> readShellCommand(String prompt) {
        FeatJAR.log().noLineBreakMessage(prompt);

        if (isWindows()) {
            String inputWindows = shellScanner.nextLine().trim();
            return inputWindows.isEmpty() ? Optional.empty() : Optional.of(inputWindows);
        }

        input = new StringBuilder();
        int key;
        historyIterator = history.listIterator(history.size());
        historyCommandLine = (history.size() > 0) ? history.get(history.size() - 1) : "";

        try {
            enterInputMode();
            while (true) {

                key = reader.read();

                if (isEnter(key)) {
                    FeatJAR.log().noLineBreakMessage("\r\n");
                    break;
                }
                if (isTabulator(key)) {
                    handleTabulatorAutoComplete();
                    continue;
                }
                if (isBackspace(key)) {
                    handleBackspaceKey();
                    continue;
                }
                if (isEscape(key)) {
                    handleEscapeKey(key);
                    continue;
                }
                handleNormalKey(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exitInputMode();
        }
        cursorX = 0;

        return input.length() == 0
                ? Optional.empty()
                : Optional.of(String.valueOf(input));
    }

    private void handleEscapeKey(int key) throws IOException {
        key = getNextKey(key);
        if (key == '[') {
            key = getNextKey(key);
            if (isPageKey(key)) {
                // ignore
                lastArrowKeyUp = false;
            } else if (isDelete(key)) {
                handleDeleteKey(key);
                lastArrowKeyUp = false;
            } else {
                handleArrowKeys(key);
            }

        } else if (input.length() != 0) {
            historyIterator = history.listIterator(history.size());
            resetInputLine();
            lastArrowKeyUp = false;
        } else {
            exitInputMode();
            throw new CancellationException(
                    "\nCommand canceled\n");
        }
    }

    private int getNextKey(int key) throws IOException {
        return reader.ready() ? reader.read() : key;
    }

    private boolean isPageKey(int key) throws IOException {
        return (key == 53 || key == 54) && getNextKey(key) == 126;
    }

    private boolean isTabulator(int key) {
        return key == 9;
    }

    private boolean isEOF(int key) {
        return key == 4;
    }

    private boolean isInterrupt(int key) {
        return key == 3;
    }

    private boolean isDelete(int key) {
        return key == 51;
    }

    private boolean isEscape(int key) {
        return key == 27;
    }

    private boolean isBackspace(int key) {
        return key == 127 || key == 8; // || isTabulator(key)
    }

    private boolean isEnter(int key) {
        return key == '\r' || key == '\n';
    }

    private void handleArrowKeys(int key) {
        switch (key) {
            case 'A':
                if (historyIterator == null) {
                    return;
                }

                if (historyIterator.hasPrevious()) {
                    historyCommandLine = historyIterator.previous();
                    moveUpHistory();
                    return;
                }

                if (!historyIterator.hasPrevious()) {
                    moveUpHistory();
                    return;
                }
            case 'B':
                if (historyIterator == null) {
                    return;
                }

                if (lastArrowKeyUp && historyIterator.hasNext()) {
                    historyCommandLine = historyIterator.next();
                }

                if (!historyIterator.hasNext()) {
                    moveOutOfHistory();
                    return;
                }

                historyCommandLine = historyIterator.next();
                moveDownHistory();
                return;

            case 'C':
                moveCursorRight();
                break;
            case 'D':
                moveCursorLeft();
                break;
        }
    }

    private void handleBackspaceKey() {
        if (input.length() != 0) {
            if (cursorX >= 0) {
                if (cursorX <= input.length() && cursorX != 0) {
                    input.deleteCharAt(cursorX - 1);
                    displayCharacters(input.toString());
                    FeatJAR.log().noLineBreakMessage("\b");
                }
                if (cursorX != 0) {
                    cursorX--;
                } // TODO 238, CursorY
            }
        }
    }

    private void handleDeleteKey(int key) throws IOException {
        key = getNextKey(key);
        if (key == '~') {
            if (input.length() != 0 && cursorX != input.length()) {
                input.deleteCharAt(cursorX);
                displayCharacters(input.toString());
            }
        }
    }

    private void handleNormalKey(int key) {
        cursorX++;

        if (input.length() == 0) {
            input.append((char) key);
        } else {
            input.insert(cursorX - 1, (char) key);
        }
        displayCharacters(input.toString());
        lastArrowKeyUp = false;
    }

    private void resetInputLine() {
        input.setLength(0);
        input.append("");
        cursorX = 0;
        displayCharacters("");
    }

    private void moveDownHistory() {
        input.setLength(0);
        input.append(historyCommandLine);
        displayCharacters(historyCommandLine);
        lastArrowKeyUp = false;
    }

    private void moveOutOfHistory() {
        resetInputLine();
        lastArrowKeyUp = false;
    }

    private void moveUpHistory() {
        input.setLength(0);
        input.append(historyCommandLine);
        cursorX = input.length() - 1;
        displayCharacters(historyCommandLine);
        lastArrowKeyUp = true;
    }

    private void moveCursorLeft() {
        if (cursorX > 0) {
            cursorX--;
            FeatJAR.log().noLineBreakMessage("\033[D"); // +"" ??
        }
    }

    private void moveCursorRight() {
        if (cursorX < input.length()) {
            cursorX++;
            FeatJAR.log().noLineBreakMessage("\033[C");
        }
    }

    /**
     *Sets the terminal into a 'raw' like mode that has no line buffer such that the shell can read a single key press, signals like CTRL+C do still work
     */
    private void enterInputMode() {
        try {
            /*
             * sh executes the command in a new console.
             * -c tells the shell to read commands from the following string.
             * stty change and print terminal line settings
             * -icanon disables the classical line buffered input mode, instead every key press gets directly send to the terminal
             * -echo has to be disabled in combination with icanon to allow ANSI escape sequences to actually to what they are supposed to do
             * (e.g. "\033[D" to move the cursor one space to the left). Otherwise the control code gets directly printed to the console
             * without executing the the ANSI escape sequence.
			*/
        	Runtime.getRuntime().exec(new String[]{"sh","-c","stty -icanon -echo </dev/tty"}).waitFor();
        } catch (InterruptedException | IOException e) {
            FeatJAR.log().error("Could not enter terminal input mode: " + e.getMessage());
        }
    }

    /**
     * Resets the the changes made in {@link Shell#enterInputMode()} and sets the terminal back into 'cooked' (normal) mode
     */
    private void exitInputMode() {
        try {
            Runtime.getRuntime()
                    .exec(new String[] {"sh", "-c", "stty sane < /dev/tty"})
                    .waitFor();
        } catch (IOException | InterruptedException e) {
            FeatJAR.log().error("Could not leave terminal input mode: " + e.getMessage());
        }
    }
    
    

    private void printArt() {
        FeatJAR.log().message(" _____             _       _    _     ____   ____   _            _  _ ");
        FeatJAR.log().message("|  ___|___   __ _ | |_    | |  / \\   |  _ \\ / ___| | |__    ___ | || |");
        FeatJAR.log().message("| |_  / _ \\ / _` || __|_  | | / _ \\  | |_) |\\___ \\ | '_ \\  / _ \\| || |");
        FeatJAR.log().message("|  _||  __/| (_| || |_| |_| |/ ___ \\ |  _ <  ___) || | | ||  __/| || |");
        FeatJAR.log().message("|_|   \\___| \\__,_| \\__|\\___//_/   \\_\\|_| \\_\\|____/ |_| |_| \\___||_||_|");
        FeatJAR.log().message("\n");
    }
}
