package Utility;

import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Delay;
import Controllers.PController;
import EV3Hardware.Robot;
import Localization.LightLocalizer;
import MeasurementDevices.Odometer;
import MeasurementDevices.OdometryCorrection;
import MeasurementDevices.OdometryDisplay;
import Navigator.Navigator;
import Navigator.ObstacleAvoider;
import ObjectLocalization.ObjectDetecter;
import Sensors.DataCollector;
import Sensors.LightSensor;
import Sensors.UltrasonicSensor;

/**
 * Class to test the Navigation, the Odometer, the WallFollower, the Object
 * Finder and the Localization labs and also collect sensor data using
 * appropriate filters
 */
public class LabsTest {

	/**
	 * ObstacleAvoider instance
	 */
	private static ObstacleAvoider		avoider;

	/**
	 * Distance from the wall for the PController
	 */
	private static final int			BANDCENTER			= 20;

	/**
	 * Margin of Error for the PController
	 */
	private static final int			BANDWIDTH			= 3;

	/**
	 * Odometry correction instance
	 */
	private static OdometryCorrection	correction;

	/**
	 * Object Finder instance
	 */
	private static ObjectDetecter		finder;

	/**
	 * Forward speed for the odometer square driver
	 */
	private static final int			FORWARD_SPEED_SD	= 250;

	/**
	 * Grabbing motor speed
	 */
	private static final int			GRABBING_SPEED		= 25;

	/**
	 * Flag indicating whether the object is grabbed
	 */
	private static boolean				isGrab;

	/**
	 * Lifting motor speed
	 */
	private static final int			LIFTING_SPEED		= 15;

	/**
	 * Light localizer instance
	 */
	private static LightLocalizer		lightLocalizer;

	/**
	 * LightSensor polling rate
	 */
	private static final int			LS_RATE				= 5;

	/**
	 * Navigation instance
	 */
	private static Navigator			nav;

	/**
	 * Odometer instance
	 */
	private static Odometer				odo;

	/**
	 * OdometryDisplay instance;
	 */
	private static OdometryDisplay		odoDisplay;

	/**
	 * PController instance
	 */
	private static PController			pControl;

	/**
	 * Rotation speed for the odometer square driver
	 */
	private static final int			ROTATION_SPEED_SD	= 150;

	/**
	 * UltrasonicSensor polling rate
	 */
	private static final int			US_RATE				= 5;

	/**
	 * @param args
	 *            Arguments
	 */
	@SuppressWarnings("nls")
	public static void main(String[] args) {

		UltrasonicSensor usFrontSampler = new UltrasonicSensor(Robot.usSensor, true, true, US_RATE);
		UltrasonicSensor usAngledSampler = new UltrasonicSensor(Robot.angledUsSensor, true, true, US_RATE);
		LightSensor lightSampler = new LightSensor(Robot.lsSensor, true, true, LS_RATE);

		// - Display each button's functionality
		Robot.textLCD.clear();
		Robot.textLCD.drawString("Left: US Data Collection", 0, 0);
		Robot.textLCD.drawString("Right: Lab testing", 0, 1);

		int button = Button.waitForAnyPress();
		if (button == Button.ID_LEFT) {
			while (button != Button.ID_ESCAPE) {

				Robot.textLCD.clear();
				Robot.textLCD.drawString("Left: US Sensor", 0, 0);
				Robot.textLCD.drawString("Right: RedMode Sensor", 0, 1);
				button = Button.waitForAnyPress();

				String filtered = (usAngledSampler.isUseFilter()) ? "filtered" : "normal";
				String filteredLight = (lightSampler.isUseFilter()) ? "filtered" : "normal";

				Robot.setSpeeds(ROTATION_SPEED_SD, -ROTATION_SPEED_SD);

				if (button == Button.ID_LEFT)
					DataCollector.collectData(usFrontSampler, "usSensor" + filtered, 1000, UltrasonicSensor.TICK_RATE);
				else if (button == Button.ID_RIGHT)
					DataCollector.collectData(lightSampler, "lightSensor" + filteredLight, 1000, LightSensor.TICK_RATE);

				Sound.beep();
			}
			// - Exit
			System.exit(0);

		} else if (button == Button.ID_RIGHT) {
			// - Initiate lab components

			odo = new Odometer(true);
			nav = new Navigator(odo);
			odoDisplay = new OdometryDisplay(odo, false);

			// - Display each button's lab test
			Robot.textLCD.clear();
			Robot.textLCD.drawString("Left: Wall Follower/Navigator", 0, 0);
			Robot.textLCD.drawString("Right: Block grabbing", 0, 1);
			Robot.textLCD.drawString("Up: Odometer", 0, 2);
			Robot.textLCD.drawString("Down: US Localizer(Rising)", 0, 3);
			Robot.textLCD.drawString("Enter: Object Finder", 0, 4);

			button = Button.waitForAnyPress();
			while (button != Button.ID_ESCAPE) {
				switch (button) {
				// - Test WallFollower or navigator
					case Button.ID_LEFT:

						Robot.textLCD.clear();
						Robot.textLCD.drawString("Left: Wall Follower", 0, 0);
						Robot.textLCD.drawString("Right: Navigator+Avoider", 0, 1);
						button = Button.waitForAnyPress();
						while (button != Button.ID_ESCAPE) {
							if (button == Button.ID_LEFT) {
								pControl = new PController(BANDCENTER, BANDWIDTH, usAngledSampler, false);
								pControl.start();
							} else if (button == Button.ID_RIGHT) {
								SimpleInterrupter si = new SimpleInterrupter();
								si.setInterrupt(false);
								avoider = new ObstacleAvoider(odo, si, usFrontSampler, usAngledSampler, false);
								avoider.start();
								Queue<Vec2D> path = new LinkedList<>();
								path.offer(Vec2D.getVector(45, 45));
								path.offer(Vec2D.getVector(0, 45));
								path.offer(Vec2D.getVector(45, 0));
								path.offer(Vec2D.getVector(0, 0));
								// path.offer(Vec2D.getVector(2, 2));
								// path.offer(Vec2D.getVector(0, 60));
								// path.offer(Vec2D.getVector(60, 0));

								while (!path.isEmpty()) {
									Navigator.followPath(nav, path, si);
								}
								avoider.stop();
							}
							button = Button.waitForAnyPress();
						}
						// - End threads
						pControl.stop();
						avoider.stop();
						break;

					// - Test block grabbing
					case Button.ID_RIGHT:
						isGrab = false;
						finder = new ObjectDetecter(false, 0);
						finder.start();
						while (!isGrab) {
							Delay.msDelay(1000);
						}
						nav.turnTo(Math.toRadians(270), true);
						Robot.liftingMotor.setSpeed(LIFTING_SPEED);
						Robot.liftingMotor.rotate(105);
						Robot.grabbingMotor.setSpeed(GRABBING_SPEED);
						Robot.grabbingMotor.rotate(45);
						Robot.liftingMotor.rotate(-105);
						finder.stop();
						break;

					// - Test US Localizer
					case Button.ID_DOWN:

						Robot.textLCD.clear();
						Robot.textLCD.drawString("Left: RISING EDGE", 0, 0);
						Robot.textLCD.drawString("Right: FALLING EDGE", 0, 1);
						Robot.textLCD.drawString("Down: Light Localization", 0, 2);

						button = Button.waitForAnyPress();
						Robot.textLCD.clear();
						odoDisplay.start();

						while (button != Button.ID_ESCAPE) {
							// - Do falling edge us localizer
							if (button == Button.ID_RIGHT) {
								// localizer = new USLocalizer(odo, usFrontSampler, LocalizationType.FALLING_EDGE);
								// localizer.doLocalization(nav);
							}
							// - Do rising edge us localizer
							else if (button == Button.ID_LEFT) {
								// localizer = new USLocalizer(odo, usFrontSampler, LocalizationType.RISING_EDGE);
								// localizer.doLocalization(nav);
							}
							// - Do light sensor localizer
							else if (button == Button.ID_DOWN) {
								lightLocalizer = new LightLocalizer(odo, lightSampler);
								lightLocalizer.doLocalization(nav);
							}

							odoDisplay.stop();
							// - Display info
							Robot.textLCD.clear();
							Robot.textLCD.drawString("Left: RISING EDGE", 0, 0);
							Robot.textLCD.drawString("Right: FALLING EDGE", 0, 1);
							Robot.textLCD.drawString("Down: Color Localization", 0, 2);
							// - Wait for user click
							button = Button.waitForAnyPress();
						}
						break;

					// - Test Odometer with or without correction
					case Button.ID_UP:
						// - Start odometer display and odometer threads
						correction = new OdometryCorrection(odo, lightSampler);

						odo.start();

						while (button != Button.ID_ESCAPE) {
							// - Display options
							Robot.textLCD.clear();
							Robot.textLCD.drawString("Left: Normal", 0, 0);
							Robot.textLCD.drawString("Right: Corretion", 0, 1);
							button = Button.waitForAnyPress();

							odoDisplay.start();

							if (button == Button.ID_LEFT) {
								// - Do square driver
								for (int i = 0; i < 4; i++) {
									Robot.rotateDistance(FORWARD_SPEED_SD, FORWARD_SPEED_SD);
									Robot.rotateAngle(ROTATION_SPEED_SD, ROTATION_SPEED_SD);
								}

							} else if (button == Button.ID_RIGHT) {

								// - Start odometer here: ******
								correction.start();
								// - Do square driver
								for (int i = 0; i < 4; i++) {
									Robot.rotateDistance(FORWARD_SPEED_SD, FORWARD_SPEED_SD);
									Robot.rotateAngle(ROTATION_SPEED_SD, ROTATION_SPEED_SD);
								}
								correction.interrupt();
							}
							odo.stop();
							odoDisplay.stop();
						}
						break;

					// - Test Object Detecter
					case Button.ID_ENTER:
						finder = new ObjectDetecter(false, 0);
						// - Start object finder thread
						finder.start();
						// - Stop object detection when we press on escape
						while (button != Button.ID_ESCAPE) {
							button = Button.waitForAnyPress();
							finder.stop();
						}
						break;

					// - Exit System
					default:
						System.exit(0);
				}

				Robot.textLCD.clear();
				Robot.textLCD.drawString("Left: Wall Follower", 0, 0);
				Robot.textLCD.drawString("Right:Navigator", 0, 1);
				Robot.textLCD.drawString("Up: Odometer", 0, 2);
				Robot.textLCD.drawString("Down: US Localizer(Rising)", 0, 3);
				Robot.textLCD.drawString("Enter: Object Finder", 0, 4);
				button = Button.waitForAnyPress();

			}
		} else
			System.exit(0);
		while (button != Button.ID_ESCAPE)
			button = Button.waitForAnyPress();
		System.exit(0);

	}

	/**
	 * Set the state that indicated whether a block is grabbed or not
	 * 
	 * @param isGrab
	 */
	public static void setGrab(boolean isGrab) {
		LabsTest.isGrab = isGrab;
	}
}
