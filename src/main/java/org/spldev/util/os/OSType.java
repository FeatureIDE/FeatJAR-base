package org.spldev.util.os;

public class OSType {

	private static String OS = System.getProperty("os.name").toLowerCase();
	public static boolean IS_WINDOWS = (OS.indexOf("win") >= 0);
	public static boolean IS_MAC = (OS.indexOf("mac") >= 0);
	public static boolean IS_UNIX = (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);;

	private OSType() {
	}
}
