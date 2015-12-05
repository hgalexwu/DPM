package ObjectLocalization;

import EV3Hardware.Robot;
import Sensors.LightSensorColor;
import Sensors.UltrasonicSensor;
import Utility.Vec2D;
import lejos.hardware.Button;
import lejos.utility.Delay;

/**
 * Class allowing to find an object
 */
public class ObjectFinder {
	/**
	 * Entry point
	 * 
	 * @param args Arguments
	 */
	@SuppressWarnings({ "nls", "boxing" })
	public static void main(String[] args) {

		// Setup ultrasonic sensor
		UltrasonicSensor usSampler = new UltrasonicSensor(	Robot.usSensor, true,
															false, 5);

		// Setup color sensor
		LightSensorColor lsSampler = new LightSensorColor(	Robot.lsSensor, true,
															false, 5);

		// - Enable the termination condition
		while (Button.readButtons() != Button.ID_ESCAPE) {
			float color = lsSampler.getSample()[0];
			int distance = usSampler.getSample()[0];

			Robot.textLCD.drawString("COLOR : " + color, 0, 0);
			Robot.textLCD.drawString("DIST  : " + distance, 0, 1);

			// - Detect a void or nothingness...
			if (Vec2D.isEqual(-1.0, color, 0.5)) {
				Robot.textLCD.drawString("NO OBJECT   ", 0, 4);
			}

			// - Detect a wooden block
			else if (Vec2D.isEqual(13.0, color, 0.5) && distance <= 5) {
				Robot.textLCD.drawString("WOODEN BLOCK", 0, 4);
			}

			// - Detect a blue block
			else if (Vec2D.isEqual(2.0, color, 0.5)) {
				Robot.textLCD.drawString("BLUE BLOCK  ", 0, 4);
			}

			// - Slow down the loop
			Delay.msDelay(10);
		}

		// - Wait for user input and localize the robot
		Button.waitForAnyPress();
	}
}
