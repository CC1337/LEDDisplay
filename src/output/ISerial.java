package output;

public interface ISerial {
		
		boolean openSerialPort();

		void closeSerialPort();
		
		void send(byte[] bytes);
	
}
