package Wifi;

import java.io.IOException;

import lejos.hardware.Button;
import EV3Hardware.Robot;

@SuppressWarnings("javadoc")
public class WifiInformation {
	// example call of the transmission protocol
	// The print function is just for debugging to make sure data is received correctly

	// *** INSTRUCTIONS ***
	// There are two variables to set manually on the EV3 client:
	// 1. SERVER_IP: the IP address of the computer running the server application
	// 2. TEAM_NUMBER: your project team number

	private static StartCorner	corner;
	private static int			homeZoneBL_X;
	private static int			homeZoneBL_Y;
	private static int			opponentHomeZoneBL_X;
	private static int			opponentHomeZoneBL_Y;
	private static int			dropZone_X;
	private static int			dropZone_Y;
	private static int			flagType;
	private static int			opponentFlagType;

	@SuppressWarnings("nls")
	private static final String	SERVER_IP	= "192.168.10.120";
	private static final int	TEAM_NUMBER	= 11;

	@SuppressWarnings({ "nls", "hiding" })
	public static int[] getInformation() {

		WifiConnection conn = null;
		try {
			conn = new WifiConnection(SERVER_IP, TEAM_NUMBER);
		} catch (@SuppressWarnings("unused") IOException e) {
			Robot.textLCD.drawString("Connection failed", 0, 8);
		}

		// example usage of Transmission class
		@SuppressWarnings("null")
		Transmission t = conn.getTransmission();
		if (t == null) {
			Robot.textLCD.drawString("Failed to read transmission", 0, 5);
		} else {
			WifiInformation.corner = t.startingCorner;
			homeZoneBL_X = t.homeZoneBL_X;
			homeZoneBL_Y = t.homeZoneBL_Y;
			opponentHomeZoneBL_X = t.opponentHomeZoneBL_X;
			opponentHomeZoneBL_Y = t.opponentHomeZoneBL_Y;
			dropZone_X = t.dropZone_X;
			dropZone_Y = t.dropZone_Y;
			flagType = t.flagType;
			opponentFlagType = t.opponentFlagType;

			// print out the transmission information
			conn.printTransmission();
		}
		// stall until user decides to end program
		Button.waitForAnyPress();
		return new int[] { corner.getId(),
						homeZoneBL_X,
						homeZoneBL_Y,
						opponentHomeZoneBL_X,
						opponentHomeZoneBL_Y,
						dropZone_X,
						dropZone_Y,
						flagType,
						opponentFlagType };
	}
}
