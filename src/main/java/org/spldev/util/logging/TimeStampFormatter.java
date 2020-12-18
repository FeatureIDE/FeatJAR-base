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
package org.spldev.util.logging;

import java.sql.*;
import java.text.*;

/**
 * Extends the standard output with time codes, indentation, and log file
 * writing.
 * 
 * @author Sebastian Krieter
 */
public class TimeStampFormatter implements Formatter {

	private static final String DATE_FORMAT_STRING = "MM/dd/yyyy-HH:mm:ss";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

	public static final String getCurTime() {
		return DATE_FORMAT.format(new Timestamp(System.currentTimeMillis()));
	}

	@Override
	public void format(StringBuilder message) {
		message.append(getCurTime());
		message.append(' ');
	}

}
