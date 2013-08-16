package effects.text;


public class PixelatedFont implements IPixelatedFont {
	
	private IFont _font;
	private int _charSpacing = 1;

	public PixelatedFont(IFont font) {
		_font = font;
	}

	
	public byte[][] toPixels(char character) {
		byte[][] result = new byte[_font.charSizeX()][_font.charSizeY()];
		String charString = _font.getCharPixelString(character);
		for(int y=0; y<_font.charSizeY(); y++) {
			for(int x=0; x<_font.charSizeX(); x++) {
				result[x][y] = (byte) (charString.charAt(y*_font.charSizeX()+x) == '1' ? 1 : 0);
			}
		}
		return result;
	}
	
	public byte[][] toPixels(String string) {
		byte[][] result = new byte[_font.charSizeX()*string.length() + (string.length()-1)*_charSpacing][_font.charSizeY()];
		for(int i=0; i<string.length(); i++) {
			byte[][] charPixels = toPixels(string.charAt(i));
			for (int x=0; x<_font.charSizeX(); x++) {
				for (int y=0; y<_font.charSizeY(); y++) {
					result[x + i*_font.charSizeX() + i*_charSpacing][y] = charPixels[x][y];
				}
			}
		}
		return result;
	}

	@Override
	public void setCharSpacing(int charSpacing) {
		_charSpacing = charSpacing;
	}


	@Override
	public int getCharSpacing() {
		return _charSpacing;
	}

}
