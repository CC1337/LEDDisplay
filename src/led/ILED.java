package led;

public interface ILED {

	int r();
	
	int g();
	
	int b();
	
	int rWithBrightnessCorrection();
	
	int gWithBrightnessCorrection();
	
	int bWithBrightnessCorrection();

	void r(int r);
	
	void g(int g);
	
	void b(int b);
	
	void w(int w);
	
	void off();
}
