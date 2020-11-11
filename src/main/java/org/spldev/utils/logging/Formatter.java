package org.spldev.utils.logging;

@FunctionalInterface
public interface Formatter {

	void format(StringBuilder message);

}
