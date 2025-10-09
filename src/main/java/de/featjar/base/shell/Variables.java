package de.featjar.base.shell;

import java.util.List;
import java.util.Optional;

public class Variables implements IShellCommand{

	@Override
	public void execute(ShellSession session, List<String> cmdParams) {
		
//		if(cmdParams.size() == 1) {
//			cmdParams.get(0).e
//		}
		
		
		
		
		session.printVariables();
		
	}
	
    @Override
    public Optional<String> getShortName() {
        return Optional.of("variables");
    }
    
    @Override
    public Optional<String> getDescription(){
    	return Optional.of("print the name and type of all session variables");
    }

}
