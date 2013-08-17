package effects.text;

public interface IFont {
	public void addChar(String newChar, String stringRepresentation);
	
	public String getCharPixelString(String c);
	
	public int charSizeX();
	
	public int charSizeY();
		
}
