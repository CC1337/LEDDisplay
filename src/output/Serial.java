package output;
//import gnu.io.*;

import java.io.IOException;
//import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class Serial implements ISerial {

	CommPortIdentifier serialPortId;
	Enumeration enumComm;
	SerialPort serialPort;
	OutputStream outputStream;
	Boolean serialPortOpen = false;

	int baudrate;
	int dataBits = SerialPort.DATABITS_8;
	int stopBits = SerialPort.STOPBITS_1;
	int parity = SerialPort.PARITY_NONE;
	String portName;
	
	public Serial(int baudrate, String portName)
	{
		this.baudrate = baudrate;
		this.portName = portName;
	}
	
	public boolean openSerialPort()
	{
		Boolean foundPort = false;
		if (serialPortOpen != false) {
			System.out.println("Serial port already opened.");
			return false;
		}
		System.out.println("Opening serial port "+ portName);
		enumComm = CommPortIdentifier.getPortIdentifiers();
		while(enumComm.hasMoreElements()) {
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (portName.contentEquals(serialPortId.getName())) {
				foundPort = true;
				break;
			}
		}
		if (foundPort != true) {
			System.out.println("Serial port not found: " + portName);
			return false;
		}
		try {
			serialPort = (SerialPort) serialPortId.open("oeffnen und Senden", 500);
		} catch (PortInUseException e) {
			System.out.println("Port belegt");
			e.printStackTrace();
		}
		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.out.println("Keinen Zugriff auf OutputStream");
			e.printStackTrace();
		}
		try {
			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch(UnsupportedCommOperationException e) {
			System.out.println("Konnte Schnittstellen-Paramter nicht setzen");
			e.printStackTrace();
		}
		
		serialPortOpen = true;
		return true;
	}

	public void closeSerialPort()
	{
		if ( serialPortOpen == true) {
			System.out.println("Closing serial port.");
			serialPort.close();
			serialPortOpen = false;
		} else {
			System.out.println("Serial port already closed.");
		}
	}
	
	public void send(byte[] bytes)
	{
		if (serialPortOpen != true) {
			System.out.println("ERROR: Serial port not opened.");
			return;
		}
		try {
			outputStream.write(bytes);
		} catch (IOException e) {
			System.out.println("Error while sending.");
			e.printStackTrace();
		}
	}
}