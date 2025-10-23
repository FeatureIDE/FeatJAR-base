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
	public static Shell shell = null;
	private ShellSession session;
	private final String osName;
	private final Scanner shellScanner;
	private List<String> history;
	private ListIterator<String> historyIterator;
	private final BufferedReader reader;
	private StringBuilder input;
	String historyCommandLine;
	private int cursorX = 0, cursorY = 0;
	private boolean lastArrowKeyUp = false;

    private static final String TERMINAL_COLOR_RED = "\033[0;31m";
    private static final String TERMINAL_COLOR_RESET = "\033[0m";


	private Shell() {
		this.session = new ShellSession();
		this.osName = System.getProperty("os.name");
		this.history = new LinkedList<String>();
		this.historyIterator = history.listIterator();

		if(isWindows()) {
			this.shellScanner = new Scanner(System.in);
			this.reader = null;
		} else {
			this.shellScanner = null;
			this.reader = new BufferedReader(new InputStreamReader(System.in));
		}
		//TODO remove the next line !
		session.put("p", Paths.get("../feature-model/testFeatureModels/basic.xml"), Path.class);
	}

	public static Shell getInstance() {
		return (shell == null) ? (shell = new Shell()) : shell;
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
			cmdArg = readCommand("$")
				    .map(c -> Arrays.stream(c.split("\\s+")).collect(Collectors.toList()))
				    .orElse(Collections.emptyList());
			} catch (CancellationException e) {
				exitInputMode();
				System.exit(0);
			}
			if(!cmdArg.isEmpty()) {
				try {
				Result<IShellCommand> command = parseCommand(cmdArg.get(0));
				cmdArg.remove(0);
					if(command.isPresent()) {
						history.add(command.get().getShortName().get() + " " + cmdArg.stream().map(String::valueOf).collect(Collectors.joining(" ")));
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
		List<IShellCommand> commands = shellCommandsExentionsPoint
				.getExtensions().stream().filter(command -> command.getShortName()
						.map(name -> name.toLowerCase().startsWith(commandString)).orElse(Boolean.FALSE))
				.collect(Collectors.toList());

		if (commands.size() > 1) {
			Map<Integer, IShellCommand> ambiguousCommands = new HashMap<Integer, IShellCommand>();
			int i = 1;

			FeatJAR.log().info
			(wrapErorrColor("Command name '%s' is ambiguous! choose one of the following %d commands (leave blank to abort): \n")
					, commandString, commands.size());

			for(IShellCommand c : commands) {
				FeatJAR.log().message(i + "." + c.getShortName().get() + " - " + c.getDescription().get());
				ambiguousCommands.put(i, c);
				i++;
			}

			String choice = readCommand("").orElse("");

			if(choice.isBlank()) {
				return Result.empty();
			}
			int parsedChoice;
			try {
				parsedChoice = Integer.parseInt(choice);
			}catch (NumberFormatException e) {
				return Result.empty(addProblem(Severity.ERROR, String.format("'%s' is no vaild number", choice), e));
			}


			for (Map.Entry<Integer, IShellCommand> entry : ambiguousCommands.entrySet()) {
	            if (Objects.equals(entry.getKey(), parsedChoice)) {
	                return Result.of(entry.getValue());
	            }
	        }
			return Result.empty(addProblem(Severity.ERROR
					, "Command name '%s' is ambiguous! It matches the following commands: \n%s and wrong number !"
					, commandString, commands.stream().map(IShellCommand::getIdentifier).collect(Collectors.joining("\n"))));
		}

		IShellCommand command = null;
		if (commands.isEmpty()) {
			Result<IShellCommand> matchingExtension = shellCommandsExentionsPoint.getMatchingExtension(commandString);
			if (matchingExtension.isEmpty()) {
				FeatJAR.log().message(Shell.wrapErorrColor("No such command '"+commandString+"'. \n <help> shows all viable commands"));
				return Result.empty(addProblem(Severity.ERROR, "No command matched the name '%s'!", commandString));
			}
			command = matchingExtension.get();
		} else {
			if(commands.get(0).getShortName().get().matches(commandString)) {
				command = commands.get(0);
				return Result.of(command);
			}
			String choice = readCommand("Do you mean: " + commands.get(0).getShortName().get() + "? (ENTER) or (a)bort\n").orElse("");
			if(choice.isEmpty()) {
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

	private boolean isWindows() {
		return osName.startsWith("Windows");
	}


    /**
     * Displays the typed characters in the console.
     * '\r' moves the cursor to the beginning of the line
     * '\u001B[2K' or '\033[2K' erases the entire line
     * '\u001B' (unicode) or '\033' (octal) for ESC work fine here
     * '\u001B[#G' moves cursor to column #
     * see for more documentation: https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797
     * @param cursor the cursor position
     * @param typedText the typed characters
     */

	private static void displayCharacters(int cursor, String typedText) {
  		FeatJAR.log().noLineBreakMessage("\r");
	    FeatJAR.log().noLineBreakMessage("\033[2K");
	    FeatJAR.log().noLineBreakMessage("$"+typedText);
	    FeatJAR.log().noLineBreakMessage("\033[" + (cursor + 2) + "G");
	  }

	public static String wrapErorrColor(String message) {
		return TERMINAL_COLOR_RED + message + TERMINAL_COLOR_RESET;
	}

	public static Optional<String> readCommand(String prompt) {
		return Shell.getInstance().readShellCommand(prompt);
	}

	private Optional<String> readShellCommand(String prompt) {
		FeatJAR.log().noLineBreakMessage(prompt);

		if(isWindows()) {
			String inputWindows = shellScanner.nextLine().trim();
			return inputWindows.isEmpty() ? Optional.empty() : Optional.of(inputWindows);
		}

		input = new StringBuilder();
		int inputCharacter;
		historyIterator = history.listIterator(history.size());
		historyCommandLine = (history.size() > 0) ? history.get(history.size() -1) : "";

		try {
			enterInputMode();
			while (true){

				inputCharacter = reader.read();

				if(inputCharacter == '\r' || inputCharacter == '\n') {
					FeatJAR.log().noLineBreakMessage("\r\n");
					break;
				}

			    if (inputCharacter == 127 || inputCharacter == 8) {
			        handleBackspaceKey();
		            continue;
			    }

			    if(inputCharacter == 27) {
			    	if(reader.ready()) {
			    		inputCharacter = reader.read();
			    	}
	                if(inputCharacter == '[') {
	                    inputCharacter = reader.read();
	                    handleArrowKeys(inputCharacter);
		                if (inputCharacter == 51) {
					    	handleDeleteKey(inputCharacter);
						    lastArrowKeyUp = false;
		                }
	                } else if (input.length() != 0){
	            		historyIterator = history.listIterator(history.size());
	            		resetInputLine();
	    			    lastArrowKeyUp = false;
	                } else {
	                    exitInputMode();
	                    throw new CancellationException("\nCommand canceled\n");
	                }
			    } else {
			    	handleNormalKey(inputCharacter);
				    lastArrowKeyUp = false;
			    }
//			    lastArrowKeyUp = false;
			}

		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			exitInputMode();
		}
		cursorX = 0;

		return input.length() == 0 ? Optional.empty() : Optional.of(String.valueOf(input));
	}

	private void handleArrowKeys(int inputCharacter) {
		switch (inputCharacter) {
		case 'A':
			if(historyIterator == null) {
				return;
			}

		    if(historyIterator.hasPrevious()) {
		    	historyCommandLine = historyIterator.previous();
		        moveUpHistory();
		        return;
		    }

			if(!historyIterator.hasPrevious()) {
		        moveUpHistory();
		        return;
			}
		case 'B':
		 	if(historyIterator == null) {
				return;
			}

			if(lastArrowKeyUp && historyIterator.hasNext()) {
				historyCommandLine = historyIterator.next();
			}

		  	if(!historyIterator.hasNext()) {
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
			if(cursorX >= 0) {
				if(cursorX <= input.length() && cursorX != 0) {
					input.deleteCharAt(cursorX-1);
					displayCharacters(cursorX, input.toString());
					FeatJAR.log().noLineBreakMessage("\b");
				}
				if(cursorX != 0) {
					cursorX--;
				}
			} 
		}
	}

	private void handleDeleteKey(int inputCharacter) throws IOException {
		if(reader.ready()) {
			inputCharacter = reader.read();
		}
		if(inputCharacter == '~') {
			handleDeleteKey();
		}
	}

	private void handleNormalKey(int inputCharacter) {
		cursorX++;

		if(input.length() == 0) {
			input.append((char) inputCharacter);
		} else {
			input.insert(cursorX-1,(char) inputCharacter);
		}
		displayCharacters(cursorX, input.toString());
	}

	private void resetInputLine() {
		input.setLength(0);
		input.append("");
		displayCharacters(cursorX, "");
	}

	private void moveDownHistory() {
		input.setLength(0);
		input.append(historyCommandLine);
		displayCharacters(cursorX, historyCommandLine);
		lastArrowKeyUp = false;
	}

	private void moveOutOfHistory() {
		resetInputLine();
		lastArrowKeyUp = false;
	}

	private void moveUpHistory() {
		input.setLength(0);
		input.append(historyCommandLine);
		displayCharacters(cursorX, historyCommandLine);
		lastArrowKeyUp = true;
	}

	private void moveCursorLeft() {
		FeatJAR.log().noLineBreakMessage("\033[D"+"");
		if(cursorX > 0) {
			cursorX--;
		}
	}

	private void moveCursorRight() {
		if (cursorX < input.length()) {
			FeatJAR.log().noLineBreakMessage("\033[C");
			cursorX++;
		}
	}

	private void handleDeleteKey() {
		if(input.length() != 0 && cursorX != input.length()) {
			input.deleteCharAt(cursorX);
			displayCharacters(cursorX, input.toString());
		}
	}

    /**
     * TODO
     */

	private void enterInputMode () {
        try {
	        Runtime.getRuntime().exec(new String[]{"sh","-c","stty -icanon -echo min 1 time 0 -isig -ixon opost onlcr </dev/tty"}).waitFor();
		} catch (InterruptedException | IOException e) {
            FeatJAR.log().error("Could not enter special terminal mode: " + e.getMessage());
		}
	}

    /**
     * TODO
     */

	private void exitInputMode() {
        try {
        	Runtime.getRuntime().exec(new String[]{"sh","-c","stty sane < /dev/tty"}).waitFor();
        } catch (IOException | InterruptedException e) {
            FeatJAR.log().error("Could not leave special terminal mode: " + e.getMessage());
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
