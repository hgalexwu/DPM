/*
 * @author Sean Lawlor
 * @date November 3, 2011
 * @class ECSE 211 - Design Principle and Methods
 * Modified by F.P. Ferrie
 * February 28, 2014
 * Changed parameters for W2014 competition
 * Modified by Francois OD
 * November 11, 2015
 * Ported to EV3 and wifi (from NXT and bluetooth)
 * Changed parameters for F2015 competition
 */
package Wifi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

/*
 * This class opens a wifi connection, waits for the data
 * and then allows access to the data after closing the wifi socket.
 * It should be used by calling the constructor which will automatically wait for
 * data without any further user command
 * Then, once completed, it will allow access to an instance of the Transmission
 * class which has access to all of the data needed
 */
@SuppressWarnings("javadoc")
public class WifiConnection {

	private Transmission trans;

	private TextLCD LCD = LocalEV3.get().getTextLCD();

	@SuppressWarnings({ "nls", "resource" })
	public WifiConnection(String serverIP, int teamNumber) throws IOException {
		this.LCD.clear();

		// Open connection to the server and data streams
		int port = 2000 + teamNumber; // semi-abritrary port number"
		this.LCD.drawString("Opening wifi connection to server at IP: " +
							serverIP, 0, 0);
		Socket socketClient = new Socket(serverIP, port);
		this.LCD.drawString("Connected to server", 0, 1);
		DataOutputStream dos = new DataOutputStream(socketClient.getOutputStream());
		DataInputStream dis = new DataInputStream(socketClient.getInputStream());

		// Wait for the server transmission to arrive
		this.LCD.drawString("Waiting from transmission...", 0, 2);
		while (dis.available() <= 0)
			try {
				Thread.sleep(10);
			} catch (@SuppressWarnings("unused") InterruptedException e) {
				assert true;
			}
		this.LCD.drawString("Receiving transmission", 0, 3);

		// Parse transmission
		this.trans = ParseTransmission.parse(dis);
		this.LCD.drawString("Finished parsing", 0, 4);

		// End the wifi connection
		dis.close();
		dos.close();
		socketClient.close();
		this.LCD.drawString("Connection terminated", 0, 5);

	}

	public Transmission getTransmission() {
		return this.trans;
	}

	@SuppressWarnings("nls")
	public void printTransmission() {
		try {
			this.LCD.clear();
			this.LCD.drawString(("Trans. Values"), 0, 0);
			this.LCD.drawString("Start: " +
								this.trans.startingCorner.toString(), 0, 1);
			this.LCD.drawString("HZ: " +this.trans.homeZoneBL_X + " " +
								this.trans.homeZoneBL_Y + " " +
								this.trans.homeZoneTR_X + " " +
								this.trans.homeZoneTR_Y, 0, 2);
			this.LCD.drawString("OHZ: " +this.trans.opponentHomeZoneBL_X +
								" " + this.trans.opponentHomeZoneBL_Y + " " +
								this.trans.opponentHomeZoneTR_X + " " +
								this.trans.opponentHomeZoneTR_Y, 0, 3);
			this.LCD.drawString("DZ: " +this.trans.dropZone_X + " " +
								this.trans.dropZone_Y, 0, 4);
			this.LCD.drawString("Flg: " +this.trans.flagType + " " +
								this.trans.opponentFlagType, 0, 5);
		} catch (@SuppressWarnings("unused") NullPointerException e) {
			this.LCD.drawString("Bad Trans", 0, 7);
		}
	}

}
