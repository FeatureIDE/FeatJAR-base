package de.featjar.base.shell;

import de.featjar.base.FeatJAR;
import de.featjar.base.extension.AExtensionPoint;

public class ShellCommands extends AExtensionPoint<IShellCommand>{
	
	public static ShellCommands getInstance() {
		return FeatJAR.extensionPoint(ShellCommands.class);
	}
}
