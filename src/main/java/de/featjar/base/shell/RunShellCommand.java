package de.featjar.base.shell;

import java.util.List;
import java.util.Optional;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.Commands;
import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.OptionList;

public class RunShellCommand implements IShellCommand {
	
	@Override
	public void execute(ShellSession session, List<String> cmdParams) {
		ICommand cliCommand = Commands.getInstance().getExtension(cmdParams.get(0)).get();
		OptionList shellOptions = cliCommand.getShellOptions(session, cmdParams.subList(1, cmdParams.size()));
		cliCommand.getOptions().forEach(o -> {
			FeatJAR.log().message(o+"="+shellOptions.getResult(o).map(String::valueOf).orElse(""));
		}
		);
		cliCommand.run(shellOptions);
	}

	@Override
	public Optional<String> getShortName() {
		return Optional.of("run");
	}

	@Override
	public Optional<String> getDescription() {
		return Optional.of("run...");
	}

}
