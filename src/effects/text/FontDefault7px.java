package effects.text;


public class FontDefault7px extends FontBase {

	public FontDefault7px() {
	
		super(5,7);
		
		// Sonderzeichenliste: http://www.bennyn.de/programmierung/java/umlaute-und-sonderzeichen-in-java.html
		
		// Grossbuchstaben
		addChar("A", "01110" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "11111" +
					 "10001" +
					 "10001");
		
		addChar("\u00C4", "10001" +
					 "00100" +
					 "01010" +
					 "10001" +
					 "11111" +
					 "10001" +
					 "10001");
		
		addChar("B", "11110" +
					 "10001" +
					 "10001" +
					 "11110" +
					 "10001" +
					 "10001" +
					 "11110");
		
		addChar("C", "01110" +
					 "10001" +
					 "10000" +
					 "10000" +
					 "10000" +
					 "10001" +
					 "01110");
		
		addChar("D", "11100" +
					 "10010" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "10010" +
					 "11100");
		
		addChar("E", "11111" +
					 "10000" +
					 "10000" +
					 "11110" +
					 "10000" +
					 "10000" +
					 "11111");
		
		addChar("F", "11111" +
					 "10000" +
					 "10000" +
					 "11110" +
					 "10000" +
					 "10000" +
					 "10000");
		
		addChar("G", "01110" +
					 "10001" +
					 "10000" +
					 "10111" +
					 "10001" +
					 "10001" +
					 "01111");
		
		addChar("H", "10001" +
					 "10001" +
					 "10001" +
					 "11111" +
					 "10001" +
					 "10001" +
					 "10001");
		
		addChar("I", "01110" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "01110");
		
		addChar("J", "00001" +
					 "00001" +
					 "00001" +
					 "00001" +
					 "00001" +
					 "10001" +
					 "01110");
		
		addChar("K", "10001" +
					 "10010" +
					 "10100" +
					 "11000" +
					 "10100" +
					 "10010" +
					 "10001");
		
		addChar("L", "10000" +
					 "10000" +
					 "10000" +
					 "10000" +
					 "10000" +
					 "10000" +
					 "11111");
		
		addChar("M", "10001" +
					 "11011" +
					 "10101" +
					 "10101" +
					 "10001" +
					 "10001" +
					 "10001");
		
		addChar("N", "10001" +
					 "10001" +
					 "11001" +
					 "10101" +
					 "10010" +
					 "10001" +
					 "10001");
		
		addChar("O", "01110" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "01110");
		
		addChar("\u00D6", "10001" +
					 "00000" +
					 "01110" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "01110");
		
		addChar("P", "11110" +
					 "10001" +
					 "10001" +
					 "11110" +
					 "10000" +
					 "10000" +
					 "10000");
		
		addChar("Q", "01110" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "10101" +
					 "10010" +
					 "01111");
		
		addChar("R", "11110" +
					 "10001" +
					 "10001" +
					 "11110" +
					 "10100" +
					 "10010" +
					 "10001");
		
		addChar("S", "01111" +
					 "10000" +
					 "10000" +
					 "01110" +
					 "00001" +
					 "00001" +
					 "11110");
		
		addChar("\u00DF", "01100" +
					 "10010" +
					 "10010" +
					 "10110" +
					 "10001" +
					 "10001" +
					 "10110");
		
		addChar("T", "11111" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "00100");
		
		addChar("U", "10001" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "01110");
		
		addChar("\u00DC", "10001" +
					 "00000" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "01110");
		
		addChar("V", "10001" +
					 "10001" +
					 "10001" +
					 "01010" +
					 "01010" +
					 "00100" +
					 "00100");
		
		addChar("W", "10001" +
					 "10001" +
					 "10001" +
					 "10101" +
					 "10101" +
					 "10101" +
					 "01010");
		
		addChar("X", "10001" +
					 "10001" +
					 "01010" +
					 "00100" +
					 "01010" +
					 "10001" +
					 "10001");
		
		addChar("Y", "10001" +
					 "10001" +
					 "10001" +
					 "01010" +
					 "00100" +
					 "00100" +
					 "00100");
		
		addChar("Z", "11111" +
					 "00001" +
					 "00010" +
					 "00100" +
					 "01000" +
					 "10000" +
					 "11111");
		
		// Kleinbuchstaben
		
		addChar("a", "00000" +
					 "00000" +
					 "01110" +
					 "00001" +
					 "01111" +
					 "10001" +
					 "01111");
		
		addChar("\u00E4", "01001" +
					 "00000" +
					 "01110" +
					 "00001" +
					 "01111" +
					 "10001" +
					 "01111");
		
		addChar("b", "11000" +
					 "01000" +
					 "01110" +
					 "01001" +
					 "01001" +
					 "01001" +
					 "01110");
		
		addChar("c", "00000" +
					 "00000" +
					 "01110" +
					 "10000" +
					 "10000" +
					 "10001" +
					 "01110");
		
		addChar("d", "00010" +
					 "00010" +
					 "01110" +
					 "10010" +
					 "10010" +
					 "10010" +
					 "01101");
		
		addChar("e", "00000" +
					 "00000" +
					 "01110" +
					 "10001" +
					 "11111" +
					 "10000" +
					 "01110");
		
		addChar("f", "00110" +
					 "01001" +
					 "11100" +
					 "01000" +
					 "01000" +
					 "01000" +
					 "01000");
		
		addChar("g", "00000" +
					 "01101" +
					 "10010" +
					 "10010" +
					 "01110" +
					 "00010" +
					 "11100");
		
		addChar("h", "11000" +
					 "01000" +
					 "01110" +
					 "01001" +
					 "01001" +
					 "01001" +
					 "01001");
		
		addChar("i", "00100" +
					 "00000" +
					 "01100" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "01110");
		
		addChar("j", "00010" +
					 "00000" +
					 "00110" +
					 "00010" +
					 "00010" +
					 "00010" +
					 "11100");
		
		addChar("k", "011000" +
					 "01000" +
					 "01001" +
					 "01010" +
					 "01100" +
					 "01010" +
					 "01001");
		
		addChar("l", "01100" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "01110");
		
		addChar("m", "00000" +
					 "00000" +
					 "11010" +
					 "10101" +
					 "10101" +
					 "10101" +
					 "10101");
		
		addChar("n", "00000" +
					 "00000" +
					 "11100" +
					 "10010" +
					 "10010" +
					 "10010" +
					 "10010");
		
		addChar("o", "00000" +
					 "00000" +
					 "01110" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "01110");
		
		addChar("\u00F6", "10001" +
					 "00000" +
					 "01110" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "01110");
		
		addChar("p", "00000" +
					 "10110" +
					 "01001" +
					 "01001" +
					 "01110" +
					 "01000" +
					 "01000");
		
		addChar("q", "00000" +
					 "01101" +
					 "10010" +
					 "10010" +
					 "01110" +
					 "00010" +
					 "00010");
		
		addChar("r", "00000" +
					 "00000" +
					 "10110" +
					 "01001" +
					 "01000" +
					 "01000" +
					 "11100");
		
		addChar("s", "00000" +
					 "00000" +
					 "01111" +
					 "10000" +
					 "01110" +
					 "00001" +
					 "11110");
		
		addChar("t", "01000" +
					 "01000" +
					 "11110" +
					 "01000" +
					 "01000" +
					 "01000" +
					 "00110");
		
		addChar("u", "00000" +
				 	 "00000" +
					 "10010" +
					 "10010" +
					 "10010" +
					 "10010" +
					 "01101");
		
		addChar("\u00FC", "10010" +
				 	 "00000" +
					 "10010" +
					 "10010" +
					 "10010" +
					 "10010" +
					 "01101");
		
		addChar("v", "00000" +
				 	 "00000" +
					 "10001" +
					 "10001" +
					 "10001" +
					 "01010" +
					 "00100");
		
		addChar("w", "00000" +
					 "00000" +
					 "10001" +
					 "10101" +
					 "10101" +
					 "01010" +
					 "01010");
		
		addChar("x", "00000" +
				 	 "00000" +
					 "10001" +
					 "01010" +
					 "00100" +
					 "01010" +
					 "10001");
		
		addChar("y", "00000" +
					 "10010" +
					 "10010" +
					 "10010" +
					 "01110" +
					 "00010" +
					 "11100");
		
		addChar("z", "00000" +
					 "00000" +
					 "11111" +
					 "00010" +
					 "00100" +
					 "01000" +
					 "11111");
		
		
		// Sonderzeichen
		
		addChar(" ", "00000" +
					 "00000" +
					 "00000" +
					 "00000" +
					 "00000" +
					 "00000" +
					 "00000");
			
		addChar("(", "00010" +
					 "00100" +
					 "01000" +
					 "01000" +
					 "01000" +
					 "00100" +
					 "00010");
		
		addChar(")", "01000" +
					 "00100" +
					 "00010" +
					 "00010" +
					 "00010" +
					 "00100" +
					 "01000");
				
		addChar("[", "01110" +
					 "01000" +
					 "01000" +
					 "01000" +
					 "01000" +
					 "01000" +
					 "01110");
		
		addChar("]", "01110" +
					 "00010" +
					 "00010" +
					 "00010" +
					 "00010" +
					 "00010" +
					 "01110");
		
		addChar("$", "01110" +
					 "10101" +
					 "10100" +
					 "01110" +
					 "00101" +
					 "10101" +
					 "01110");
		
		addChar("&", "00000" +
					 "01000" +
					 "10100" +
					 "01000" +
					 "10101" +
					 "10010" +
					 "01101");
		
		addChar(".", "00000" +
					 "00000" +
					 "00000" +
					 "00000" +
					 "01100" +
					 "01100" +
					 "00000");
		
		addChar(",", "00000" +
					 "00000" +
					 "00000" +
					 "00000" +
					 "01100" +
					 "00100" +
					 "01000");
		
		addChar(":", "00000" +
					 "01100" +
					 "01100" +
					 "00000" +
					 "01100" +
					 "01100" +
					 "00000");
	
		addChar(";", "00000" +
					 "01100" +
					 "01100" +
					 "00000" +
					 "01100" +
					 "00100" +
					 "01000");
		
		addChar("!", "01000" +
					 "01000" +
					 "01000" +
					 "01000" +
					 "01000" +
					 "00000" +
					 "01000");
		
		addChar("?", "01110" +
					 "10001" +
					 "00001" +
					 "00110" +
					 "00100" +
					 "00000" +
					 "00100");
		
		addChar("-", "00000" +
					 "00000" +
					 "00000" +
					 "01110" +
					 "00000" +
					 "00000" +
					 "00000");
		
		addChar("=", "00000" +
					 "00000" +
					 "11111" +
					 "00000" +
					 "11111" +
					 "00000" +
					 "00000");
		
		addChar("_", "00000" +
					 "00000" +
					 "00000" +
					 "00000" +
					 "00000" +
					 "00000" +
					 "11111");
		
		addChar("~", "00000" +
					 "00000" +
					 "00000" +
					 "11111" +
					 "00000" +
					 "00000" +
					 "00000");
		
		addChar("/", "00010" +
					 "00010" +
					 "00100" +
					 "00100" +
					 "01000" +
					 "01000" +
					 "01000");
		
		addChar("\\", "01000" +
					  "01000" +
				  	  "00100" +
					  "00100" +
					  "00010" +
					  "00010" +
					  "00010");
		
		addChar("\u00BA", "00111" +
					 "00101" +
					 "00111" +
					 "00000" +
					 "00000" +
					 "00000" +
					 "00000");
		
		addChar("\'", "01000" +
					  "01000" +
					  "00000" +
					  "00000" +
					  "00000" +
					  "00000" +
					  "00000");
	
		addChar("+", "00000" +
					 "00100" +
					 "00100" +
					 "11111" +
					 "00100" +
					 "00100" +
					 "00000");
		
		addChar("*", "01010" +
					 "00100" +
					 "01010" +
					 "00000" +
					 "00000" +
					 "00000" +
					 "00000");
	
		// Zahlen	
		addChar("1", "00100" +
					 "01100" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "00100" +
					 "01110");
		
		addChar("2", "01110" +
					 "10001" +
					 "00001" +
					 "00010" +
					 "00100" +
					 "01000" +
					 "11111");
		
		addChar("3", "11111" +
					 "00010" +
					 "00100" +
					 "00010" +
					 "00001" +
					 "10001" +
					 "01110");
		
		addChar("4", "00010" +
					 "00110" +
					 "01010" +
					 "10010" +
					 "11111" +
					 "00010" +
					 "00010");
		
		addChar("5", "11111" +
					 "10000" +
					 "11110" +
					 "10001" +
					 "00001" +
					 "00001" +
					 "01110");
		
		addChar("6", "00010" +
					 "00100" +
					 "01000" +
					 "11110" +
					 "10001" +
					 "10001" +
					 "01110");
		
		addChar("7", "11111" +
					 "00001" +
					 "00010" +
					 "00100" +
					 "00100" +
					 "01000" +
					 "01000");
		
		addChar("8", "01110" +
					 "10001" +
					 "10001" +
					 "01110" +
					 "10001" +
					 "10001" +
					 "01110");
		
		addChar("9", "01110" +
					 "10001" +
					 "10001" +
					 "01111" +
					 "00010" +
					 "00100" +
					 "01000");
		
		addChar("0", "01110" +
					 "10001" +
					 "10011" +
					 "10101" +
					 "11001" +
					 "10001" +
					 "01110");
		

		

	}
	
}
