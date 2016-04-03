package helper;

public final class Helper {
	
	private static final boolean IsWindows = System.getProperty("os.name").startsWith("Windows");
	
	private Helper() {
		
	}

	public static boolean isWindows() {
		return IsWindows;
	}
	
	public static int getArrayAverage(int[] array) {
		int sum = 0;
		for(int i=0; i < array.length ; i++)
			sum += array[i];
		return sum/array.length;
	}
	
	public static String getSpaces(int count) {
		String result = "";
		for (int i = 0; i < count; i++)
			result += " ";
		return result;
	}


}
