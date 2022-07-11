/* -----------------------------------------------------------------------------
 * util - Common utilities and data structures
 * Copyright (C) 2020 Sebastian Krieter
 * 
 * This file is part of util.
 * 
 * util is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 * 
 * util is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with util. If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/FeatJAR/util> for further information.
 * -----------------------------------------------------------------------------
 */
package de.featjar.util.logging;

import java.sql.*;
import java.text.*;

/**
 * Prepends log output with time stamps.
 *
 * @author Sebastian Krieter
 */
public class TimeStampFormatter implements Formatter {

	private static final String DATE_FORMAT_STRING = "MM/dd/yyyy-HH:mm:ss";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

	public static final String getCurrentTime() {
		return DATE_FORMAT.format(new Timestamp(System.currentTimeMillis()));
	}

	@Override
	public void format(StringBuilder message) {
		message.append(getCurrentTime());
		message.append(' ');
	}

}
