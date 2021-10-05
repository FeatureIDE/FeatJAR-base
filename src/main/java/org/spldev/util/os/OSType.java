package org.spldev.util.os;

public final class OSType {

	public static final boolean IS_WINDOWS;
	public static final boolean IS_MAC;
	public static final boolean IS_UNIX;
	static {
		final String OS = System.getProperty("os.name").toLowerCase();
		IS_WINDOWS = OS.matches(".*(win).*");
		IS_MAC = OS.matches(".*(mac).*");
		IS_UNIX = OS.matches(".*(nix|nux|aix).*");
	}

	private OSType() {
	}

}
