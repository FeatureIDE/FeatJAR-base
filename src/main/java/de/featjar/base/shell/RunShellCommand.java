package de.featjar.base.shell;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.Commands;
import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.OptionList;
import de.featjar.base.data.Result;

public class RunShellCommand implements IShellCommand {
	
	@Override
	public void execute(ShellSession session, List<String> cmdParams) {
		
		if(cmdParams.isEmpty()) {
			FeatJAR.log().info(String.format("Usage: %s", getDescription().orElse("")));
			return;
		} 	
		try {
			Result<ICommand> cliCommand = Commands.getInstance().getExtension(cmdParams.get(0));
			
			if(cliCommand.isEmpty()) {
				FeatJAR.log().error(String.format("Command '%s' not found", cmdParams.get(0)));
				return;
			}
			OptionList shellOptions = cliCommand.get().getShellOptions(session, cmdParams.subList(1, cmdParams.size()));
			//TODO Alter options 
			cliCommand.get().getOptions().forEach(o -> {
				FeatJAR.log().message(o+"="+shellOptions.getResult(o).map(String::valueOf).orElse(""));
			});			
			cliCommand.get().run(shellOptions);
			
		} catch (IllegalArgumentException iae) {
			FeatJAR.log().error(iae.getMessage());
		}
	}

	@Override
	public Optional<String> getShortName() {
		return Optional.of("run");
	}

	@Override
	public Optional<String> getDescription() {
		return Optional.of("run... WIP");
	}

}
