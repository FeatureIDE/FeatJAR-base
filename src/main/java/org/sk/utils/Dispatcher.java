package org.sk.utils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Calls the main method of the class that is provided as the first parameter.
 * If there are multiple main methods within an executable jar file, use this
 * class as main method in the manifest. Then any main method can be called
 * without changing the manifest or setting the class path manually.
 * 
 * @author Sebastian Krieter
 */
public class Dispatcher {

	public static void main(final String[] args) throws Exception {
		if (args.length < 1) {
			throw new IllegalArgumentException("The first argument must be the name of the main class!");
		}

		final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
		final Class<?> loadClass = systemClassLoader.loadClass(args[0]);
		final Method method = loadClass.getMethod("main", String[].class);

		method.invoke(null, new Object[] { Arrays.copyOfRange(args, 1, args.length) });
	}

}
