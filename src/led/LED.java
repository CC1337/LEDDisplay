package led;

public class LED implements ILED{

	private int r = 0;
	private int g = 0;
	private int b = 0;
	
	// Get
	public int r() {
		return r;
	}
	
	public int g() {
		return g;
	}
	
	public int b() {
		return b;
	}
	
	// Set
	public void r(int r) {
		if (isValidValue(r))
			this.r = r;	
	}
	
	public void g(int g) {
		if (isValidValue(g))
			this.g = g;	
	}
	
	public void b(int b) {
		if (isValidValue(b))
			this.b = b;		
	}
	
	public void w(int w) {
		r(w);
		g(w);
		b(w);
	}
	
	public void off() {
		w(0);
	}
	
	// Validation
	private boolean isValidValue(int val) {
		return (b >= 0 && b <= 255); 
	}
	
}
