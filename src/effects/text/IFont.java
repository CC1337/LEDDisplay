package effects.text;

public interface IFont {
	public void addChar(char newChar, String stringRepresentation);
	
	public String getCharPixelString(char c);
	
	public int charSizeX();
	
	public int charSizeY();
		
}
