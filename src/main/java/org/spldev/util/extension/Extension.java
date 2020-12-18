/* -----------------------------------------------------------------------------
 * Util-Lib - Miscellaneous utility functions.
 * Copyright (C) 2020  Sebastian Krieter
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
package org.spldev.util.extension;

/**
 * An extension containing a unique ID and a method for initialization
 *
 * @author Sebastian Krieter
 */
public interface Extension {

	/**
	 * @return the unique ID of this extension.
	 */
	String getId();

	/**
	 * Is called, when the extension is loaded for the first time by an
	 * {@link ExtensionPoint}.
	 *
	 * @return {@code true} if the initialization was successful, {@code false}
	 *         otherwise.
	 */
	default boolean initExtension() {
		return true;
	}

}
