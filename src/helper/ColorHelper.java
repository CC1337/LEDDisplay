package helper;

public final class ColorHelper {

	private ColorHelper() {
		
	}
	
	public static int blend(int baseColor, int overlayColor, double alpha) {
		return (int)Math.round(baseColor * (1.0-alpha) + overlayColor * alpha);
	}
}
