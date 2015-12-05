package Navigator;

import java.util.LinkedList;
import java.util.Queue;

import EV3Hardware.Robot;
import MeasurementDevices.Odometer;
import MeasurementDevices.OdometryDisplay;
import Utility.SimpleInterrupter;
import Utility.Vec2D;
import lejos.hardware.Button;

/**
 * Class demonstrating the functionality of the navigator
 */
public class Navigation {

	/**
	 * Entry point of the navigation program
	 * 
	 * @param args Arguments sent to the program
	 */
	@SuppressWarnings("nls")
	public static void main(String[] args) {
		int buttonChoice;

		do {
			// - Clear the display
			Robot.textLCD.clear();

			// - Ask the user whether the motors should drive in a square or float
			Robot.textLCD.drawString("< Left | Right >", 0, 0);
			Robot.textLCD.drawString("       |        ", 0, 1);
			Robot.textLCD.drawString(" Float | Navigate  ", 0, 2);
			Robot.textLCD.drawString("motors | to a   ", 0, 3);
			Robot.textLCD.drawString("       | waypoint ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT &&
					buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			Robot.leftMotor.forward();
			Robot.leftMotor.flt();
			Robot.rightMotor.forward();
			Robot.rightMotor.flt();

			Odometer odometer = new Odometer(true);
			@SuppressWarnings("unused")
			OdometryDisplay odometryDisplay = new OdometryDisplay(	odometer,
																	true);

		} else {
			Odometer odometer = new Odometer(true);
			@SuppressWarnings("unused")
			OdometryDisplay odometryDisplay = new OdometryDisplay(	odometer,
																	true);
			Navigator navigator = new Navigator(odometer);
			// UltrasonicSensor usSampler = new UltrasonicSensor( Robot.usSensor,
			// true, false, 5);

			Queue<Vec2D> path = new LinkedList<>();
			// path.offer(Vec2D.getVector(15, 15));
			// path.offer(Vec2D.getVector(0, 15));
			// path.offer(Vec2D.getVector(15, 0));
			// path.offer(Vec2D.getVector(0, 0));
			// path.offer(Vec2D.getVector(2, 2));
			path.offer(Vec2D.getVector(0, 60));
			path.offer(Vec2D.getVector(60, 0));

			SimpleInterrupter pSInter = new SimpleInterrupter();
			pSInter.setInterrupt(false);

			while (!path.isEmpty()) {
				Navigator.followPath(navigator, path, new SimpleInterrupter());
			}
		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			assert(true);
		System.exit(0);
	}
}
