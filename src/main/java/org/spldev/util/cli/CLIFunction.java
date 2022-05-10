/* -----------------------------------------------------------------------------
 * Util Lib - Miscellaneous utility functions.
 * Copyright (C) 2021-2022  Sebastian Krieter
 * 
 * This file is part of Util Lib.
 * 
 * Util Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Util Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Util Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/utils> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.util.cli;

import org.spldev.util.extension.Extension;

import java.util.List;

/**
 * A function of FeatureIDE that can be accessed via the {@link CLI}.
 *
 * @author Sebastian Krieter
 */
public interface CLIFunction extends Extension {

	default String getName() {
		return getIdentifier();
	}

	void run(List<String> args);

	default String getDescription() {
		return "";
	}

	default String getHelp() {
		return "No help is available for this command.";
	}

}
