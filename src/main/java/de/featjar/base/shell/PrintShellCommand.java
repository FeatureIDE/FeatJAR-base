package de.featjar.base.shell;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.featjar.base.FeatJAR;
import de.featjar.base.tree.structure.ITree;


public class PrintShellCommand implements IShellCommand {

	@Override
	public void execute(ShellSession session, List<String> cmdParams) {
		//  TODO layer for electing the type	
		
		if(cmdParams.isEmpty()) {
			session.printVariables();
			cmdParams = Shell.readCommand("Enter the variable names you want to print or leave blank to abort:")
				    .map(c -> Arrays.stream(c.toLowerCase().split("\\s+")).collect(Collectors.toList()))
				    .orElse(Collections.emptyList());
		}
			
		cmdParams.forEach(e -> {
		    session.getElement(e)
		        .ifPresentOrElse(m -> {
		        	FeatJAR.log().info(e + ":");
		        	printMap(m);
		        }, () -> FeatJAR.log().error("Could not find a variable named " + e));
		});	
	}
	
	private void printMap(Object v) {
			if (v instanceof ITree<?>) {
				FeatJAR.log().info(((ITree<?>) v).print());
			} else {
				FeatJAR.log().info(v);
			}
			FeatJAR.log().info("");
	}
	
    public Optional<String> getShortName() {
        return Optional.of("print");
    }

    public Optional<String> getDescription(){
    	return Optional.of("print the content of variables - <cmd> <name> ...");
    }
}
