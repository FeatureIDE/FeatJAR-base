package de.featjar.base.shell;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.featjar.base.FeatJAR;

public class DeleteShellCommand implements IShellCommand {

	@Override
	public void execute(ShellSession session, List<String> cmdParams) {

		if (cmdParams.isEmpty()) {
			session.printVariables();
			cmdParams = Shell.readCommand("Enter the variable names you want to delete or leave blank to abort:")
					.map(c -> Arrays.stream(c.split("\\s+")).collect(Collectors.toList()))
					.orElse(Collections.emptyList());
		}

		cmdParams.forEach(e -> {
			session.remove(e).ifPresentOrElse(a -> FeatJAR.log().message("Removing of " + e + " successful"),
					() -> FeatJAR.log().error("Could not find a variable named " + e));
		});
	}

	@Override
	public Optional<String> getShortName() {
		return Optional.of("delete");
	}

	@Override
	public Optional<String> getDescription() {
		return Optional.of("delete session variables - <cmd> <name> ...");
	}

}
