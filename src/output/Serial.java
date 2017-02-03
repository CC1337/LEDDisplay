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
			System.err.println("ERROR: Serial port " + portName + " not opened. Retry alternate port after 1 second.");
			delayedSwapSerialPort();
			if (!serialPortOpen)
				return;
		}
		try {
			outputStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error while sending on serial port " + portName + ". Trying alternate port in 1 second.");
			delayedSwapSerialPort();
		}
	}
}