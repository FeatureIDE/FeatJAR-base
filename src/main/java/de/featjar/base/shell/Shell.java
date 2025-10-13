package de.featjar.base.shell;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Problem.Severity;
import de.featjar.base.data.Result;

public class Shell {

	public static Shell shell = null;
	ShellSession session = new ShellSession();
	private static final Scanner shellScanner = new Scanner(System.in);

	private Shell() {
		FeatJAR.initialize(FeatJAR.shellConfiguration());
		printArt();		
		new HelpShellCommand().printCommands();
		run();
	}

	public static Shell getInstance() {
		return (shell == null) ? (shell = new Shell()) : shell;
	}

	public static void main(String[] args) {
		Shell.getInstance();
	}

	private void run() {
		while (true) {
//			List<String> cmdArg = Arrays.stream(readCommand("$").split("\\s+")).collect(Collectors.toList());
			List<String> cmdArg = readCommand("$")
				    .map(c -> Arrays.stream(c.split("\\s+")).collect(Collectors.toList()))
				    .orElse(Collections.emptyList());
			
			if(!cmdArg.isEmpty()) {
				Result<IShellCommand> command = parseCommand(cmdArg.get(0));
				cmdArg.remove(0);
				command.ifPresent(cmd -> cmd.execute(session, cmdArg));
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
			Map<Integer, IShellCommand> temp = new HashMap<Integer, IShellCommand>();
			int i = 1;
			
			FeatJAR.log().info
			("Command name %s is ambiguous! choose one of the following %d commands: \n",	commandString, commands.size());

			for(IShellCommand c : commands) {
				FeatJAR.log().info(i + "." + c.getShortName().get() + " - " + c.getDescription().get());
				temp.put(i, c);
				i++;
			}
			
			String choice = readCommand("").orElse("");
			
			for (Map.Entry<Integer, IShellCommand> entry : temp.entrySet()) {
	            if (Objects.equals(entry.getKey(), Integer.parseInt(choice))) {
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
				FeatJAR.log().info("No such command '%s'. \n <help> shows all viable commands", commandString);
				
				return Result.empty(addProblem(Severity.ERROR, "No command matched the name '%s'!", commandString));
			}
			command = matchingExtension.get();
		} else {
			if(commands.get(0).getShortName().get().matches(commandString)) {
				command = commands.get(0);
				return Result.of(command);
			}
			String choice = readCommand("Do you mean: " + commands.get(0).getShortName().get() + "? (ENTER) or (a)bort").orElse("");
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
	
	public static Optional<String> readCommand(String prompt) {
		FeatJAR.log().info(prompt);
		String input = shellScanner.nextLine().trim();
		return input.isEmpty() ? Optional.empty() : Optional.of(input);
	}

	public static void printArt() {
		FeatJAR.log().info(" _____             _       _    _     ____   ____   _            _  _ ");
		FeatJAR.log().info("|  ___|___   __ _ | |_    | |  / \\   |  _ \\ / ___| | |__    ___ | || |");
		FeatJAR.log().info("| |_  / _ \\ / _` || __|_  | | / _ \\  | |_) |\\___ \\ | '_ \\  / _ \\| || |");
		FeatJAR.log().info("|  _||  __/| (_| || |_| |_| |/ ___ \\ |  _ <  ___) || | | ||  __/| || |");
		FeatJAR.log().info("|_|   \\___| \\__,_| \\__|\\___//_/   \\_\\|_| \\_\\|____/ |_| |_| \\___||_||_|");
		FeatJAR.log().info("\n");
	}
}
