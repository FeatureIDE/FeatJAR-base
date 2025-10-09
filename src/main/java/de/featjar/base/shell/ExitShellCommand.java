package de.featjar.base.shell;

import java.util.List;
import java.util.Optional;

public class ExitShellCommand implements IShellCommand {
	
	@Override
	public void execute(ShellSession session, List<String> cmdParams) {		
		System.exit(0);
	}	
	
    @Override
    public Optional<String> getShortName() {
        return Optional.of("exit");
    }
    
    @Override
    public Optional<String> getDescription(){
    	return Optional.of("leave shell");
    }
}
