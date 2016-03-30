package helper;

public final class Helper {
	
	private static final boolean IsWindows = System.getProperty("os.name").startsWith("Windows");
	
	private Helper() {
		
	}

	public static boolean isWindows() {
		return IsWindows;
	}
}
