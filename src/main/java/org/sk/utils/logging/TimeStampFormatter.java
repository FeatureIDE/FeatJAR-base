package org.sk.utils.logging;

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
