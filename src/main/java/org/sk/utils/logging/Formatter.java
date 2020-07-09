package org.sk.utils.logging;

@FunctionalInterface
public interface Formatter {

	void format(StringBuilder message);

}
