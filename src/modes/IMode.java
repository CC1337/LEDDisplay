package modes;

public interface IMode extends Runnable {
	
	public String modeName();

	public void abort();
	
	public void end();
	
	public void nextConfig();
		
}
