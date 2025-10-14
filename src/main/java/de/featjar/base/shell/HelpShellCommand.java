package de.featjar.base.shell;

import java.util.List;
import java.util.Optional;

import de.featjar.base.FeatJAR;

public class HelpShellCommand implements IShellCommand {
	
	@Override
	public void execute(ShellSession session, List<String> cmdParams) {
		printCommands();
	}
	
	public void printCommands() {
		FeatJAR.log().message("Interactive shell - supported commands are (capitalization is not taken into account):\n");
		FeatJAR.extensionPoint(ShellCommands.class).getExtensions()
			.stream().map(c -> c.getShortName().orElse("")
			.concat(" - " + c.getDescription().orElse("")))
			.forEach(FeatJAR.log()::message);		
		FeatJAR.log().message("\n");
	}
	
    @Override
    public Optional<String> getShortName() {
        return Optional.of("help");
    }
    
    @Override
    public Optional<String> getDescription(){
    	return Optional.of("print all commads");
    }
}
