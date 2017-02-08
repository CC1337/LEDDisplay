package output;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.Enumeration;
import java.util.logging.Logger;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class Serial implements ISerial {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private CommPortIdentifier serialPortId;
	private Enumeration enumComm;
	private SerialPort serialPort;
	private OutputStream outputStream;
	private Boolean serialPortOpen = false;

	private int baudrate;
	private int dataBits = SerialPort.DATABITS_8;
	private int stopBits = SerialPort.STOPBITS_1;
	private int parity = SerialPort.PARITY_NONE;
	private String portName;
	private String secondPortName;
	private String firstPortName;
	
	public Serial(int baudrate, String firstPortName, String secondPortName)
	{
		this.baudrate = baudrate;
		this.portName = firstPortName;
		this.firstPortName = firstPortName;
		this.secondPortName = secondPortName;
	}
	
	public boolean openSerialPort()
	{
		Boolean foundPort = false;
		if (serialPortOpen != false) {
			LOGGER.info("Serial port " + portName + " already opened.");
			return false;
		}
		
		LOGGER.info("Opening serial port "+ portName);
		
		enumComm = CommPortIdentifier.getPortIdentifiers();
		while(enumComm.hasMoreElements()) {
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (portName.contentEquals(serialPortId.getName())) {
				foundPort = true;
				break;
			}
		}
		if (foundPort != true) {
			LOGGER.warning("Serial port not found: " + portName);
			return false;
		}
		try {
			serialPort = (SerialPort) serialPortId.open("oeffnen und Senden", 500);
		} catch (PortInUseException e) {
			LOGGER.warning("Serial port " + portName + " busy");
			e.printStackTrace();
		}
		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			LOGGER.warning("No access to serial port output stream on " + portName);
			e.printStackTrace();
		}
		try {
			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch(UnsupportedCommOperationException e) {
			LOGGER.warning("Can't set serial port parameters on " + portName);
			e.printStackTrace();
		}
		
		LOGGER.info("Serial port " + portName + " opened successfully.");
		serialPortOpen = true;
		return true;
	}

	public void closeSerialPort()
	{
		if ( serialPortOpen == true) {
			LOGGER.info("Closing serial port " + portName);
			serialPort.close();
			serialPortOpen = false;
		} else {
			LOGGER.info("Serial port " + portName + " already closed.");
		}
	}
	
	private void delayedSwapSerialPort() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		swapSerialPort();
	}
	
	private void swapSerialPort() {
		closeSerialPort();
		if (portName.equals(firstPortName))
			portName = secondPortName;
		else
			portName = firstPortName;
		openSerialPort();
	}
	
	public void send(byte[] bytes)
	{
		if (!serialPortOpen) {
			LOGGER.severe("ERROR: Serial port " + portName + " not opened. Retry alternate port after 1 second.");
			delayedSwapSerialPort();
			if (!serialPortOpen)
				return;
		}
		try {
			outputStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.severe("Error while sending on serial port " + portName + ". Trying alternate port in 1 second.");
			delayedSwapSerialPort();
		}
	}
}