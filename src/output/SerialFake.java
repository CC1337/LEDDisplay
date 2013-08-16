package output;

public class SerialFake implements ISerial {

	@Override
	public boolean openSerialPort() {
		return true;
	}

	@Override
	public void closeSerialPort() {
	}

	@Override
	public void send(byte[] bytes) {
	}

}
