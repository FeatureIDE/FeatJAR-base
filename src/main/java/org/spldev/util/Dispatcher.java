/* -----------------------------------------------------------------------------
 * Util-Lib - Miscellaneous utility functions.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Util-Lib.
 * 
 * Util-Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util-Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util-Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util;

import java.lang.reflect.*;
import java.util.*;

import org.spldev.util.extension.*;

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
		ExtensionLoader.load();

		final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
		final Class<?> loadClass = systemClassLoader.loadClass(args[0]);
		final Method method = loadClass.getMethod("main", String[].class);

		method.invoke(null, (Object) Arrays.copyOfRange(args, 1, args.length));
	}

}
