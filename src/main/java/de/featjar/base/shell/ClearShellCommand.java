package de.featjar.base.shell;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.featjar.base.FeatJAR;

public class ClearShellCommand implements IShellCommand {

	@Override
	public void execute(ShellSession session, List<String> cmdParams) {		
		String choice = Shell.readCommand("Clearing the entire session. Proceed ? (y)es (n)o")
				.orElse("").toLowerCase().trim();
		
			if(Objects.equals("y", choice)) {
				session.clear();
				FeatJAR.log().info("Clearing successful");
			} else if(Objects.equals("n", choice)) {
				FeatJAR.log().info("Clearing aborted");
			}
		}	
	
	@Override
    public Optional<String> getShortName() {
        return Optional.of("clear");
    }
	
	@Override
    public Optional<String> getDescription(){
    	return Optional.of("delete the entire session");
    }
	
}
