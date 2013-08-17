package effects.text;

import java.util.HashMap;

public abstract class FontBase implements IFont {
	
	private HashMap<String, String> _charList;
	private int _charSizeX;
	private int _charSizeY;
	
	public FontBase(int charSizeX, int charSizeY) {
		_charSizeX = charSizeX;
		_charSizeY = charSizeY;
		_charList = new HashMap<String, String>();
	}
	
	public void addChar(String newChar, String stringRepresentation) {
		if (!_charList.containsKey(newChar))
			_charList.put(newChar, stringRepresentation);
	}
	
	public String getCharPixelString(String c) {
		if (_charList.containsKey(c))
			return _charList.get(c);
		return charNotFoundString();
	}
	
	public int charSizeX() {
		return _charSizeX;
	}
	
	public int charSizeY() {
		return _charSizeY;
	}
	
	private String charNotFoundString() {
		String result = "";
		for (int i=0; i<_charSizeX*_charSizeY; i++) {
			result += i%2 == 0 ? "1" : "0";			
		}
		return result;
	}
}
