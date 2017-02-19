package modes;

public interface IMode extends Runnable {
	
	public String modeName();

	public void abort();
	
	public void end();
	
	public void changeConfig(String newConfigFileName);
	
	public void buttonPressedShort();
	
	public void buttonPressedLong();
		
}
