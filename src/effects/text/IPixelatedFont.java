package effects.text;

public interface IPixelatedFont {

	public byte[][] toPixels(char c);
	
	public byte[][] toPixels(String s);
	
	public void setCharSpacing(int charSpacing);
	
	public int getCharSpacing();
}
