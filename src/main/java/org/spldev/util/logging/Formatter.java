package org.spldev.util.logging;

@FunctionalInterface
public interface Formatter {

	void format(StringBuilder message);

}
