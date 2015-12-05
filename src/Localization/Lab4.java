package Localization;

import EV3Hardware.Robot;
import MeasurementDevices.Odometer;
import Navigator.Navigator;
import Sensors.LightSensor;
import Sensors.UltrasonicSensor;
import lejos.hardware.Button;

/**
 * Lab 4
 */
public class Lab4 {

	/**
	 * Entry point
	 * 
	 * @param args Arguments
	 */
	@SuppressWarnings("nls")
	public static void main(String[] args) {

		// Setup ultrasonic sensor
		UltrasonicSensor usSampler = new UltrasonicSensor(	Robot.usSensor, true,
															false, 5);

		// Setup color sensor
		LightSensor lsSampler = new LightSensor(Robot.lsSensor, true, false, 5);

		// Setup the odometer and display
		Odometer odo = new Odometer(true);

		// Set the navigator
		Navigator nav = new Navigator(odo);

		// Type of localization to use
		USLocalizer.LocalizationType locType = USLocalizer.LocalizationType.FALLING_EDGE;

		// Select the type of localization
		Robot.textLCD.clear();
		Robot.textLCD.drawString("Left :Falling Edge", 0, 0);
		Robot.textLCD.drawString("Right:Rising Edge", 0, 1);
		int bt = Button.waitForAnyPress();
		if (bt == Button.ID_LEFT)
			locType = USLocalizer.LocalizationType.FALLING_EDGE;
		else if (bt == Button.ID_RIGHT)
			locType = USLocalizer.LocalizationType.RISING_EDGE;

		Robot.textLCD.clear();

		LCDInfo lcd = new LCDInfo(odo);
		assert(lcd != null);

		// - Perform the ultrasonic localization
		USLocalizer usl = new USLocalizer(odo, usSampler, locType);
		usl.doLocalization(nav);

		// - Perform the light sensor localization
		LightLocalizer lsl = new LightLocalizer(odo, lsSampler);
		lsl.doLocalization(nav);

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			assert(true);
		System.exit(0);

	}
}
