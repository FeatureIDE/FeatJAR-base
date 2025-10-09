package de.featjar.base.shell;

import java.util.List;
import java.util.Optional;

import de.featjar.base.extension.IExtension;

public interface IShellCommand extends IExtension{ 
	
	void execute(ShellSession session, List<String> cmdParams);
	
    /**
     * {@return this command's short name, if any} The short name can be used to call this command from the CLI.
     */
    default Optional<String> getShortName() {
        return Optional.empty();
    }
    
    /**
     * {@return this command's description name, if any}
     */
    default Optional<String> getDescription(){
    	return Optional.empty();
    }
}
