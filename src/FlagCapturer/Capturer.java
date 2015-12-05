package FlagCapturer;

import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Delay;
import EV3Hardware.Robot;
import Localization.ImprovedUsLocalizer;
import Localization.ImprovedUsLocalizer.LocalizationType;
import Localization.LightLocalizer;
import MeasurementDevices.Odometer;
import Navigator.Navigator;
import Navigator.ObstacleAvoider;
import ObjectLocalization.ObjectDetecter;
import Sensors.LightSensor;
import Sensors.UltrasonicSensor;
import Utility.SimpleInterrupter;
import Utility.Vec2D;
import Wifi.WifiInformation;

/**
 * Class implementing the goal of the project
 */
public class Capturer {

	/**
	 * ObstacleAvoider instance
	 */
	private static ObstacleAvoider		avoider;

	/**
	 * Margin of error for vec2D in range function in order to take into account NOISE
	 */
	private final static int			BLOCK_ERROR		= 2;

	/**
	 * Starting corner. 1 -> 0,0 2 -> 300,0 3 -> 300,300 4 -> 0,300
	 */
	public static int					corner			= 0;

	/**
	 * X coordinate of dropzone
	 */
	public static int					dropZone_X;

	/**
	 * Y coordinate of dropzone
	 */
	public static int					dropZone_Y;

	/**
	 * Object Finder instance
	 */
	private static ObjectDetecter		finder;

	/**
	 * Flag type matching color code
	 */
	public static int					flagType		= 0;

	/**
	 * Grabbing speed of them motors to grab the block
	 */
	private static final int			GRABBING_SPEED	= 30;

	/**
	 * X coordinate of homezone
	 */
	public static int					homeZoneBL_X;

	/**
	 * Y coordinate of home zone
	 */
	public static int					homeZoneBL_Y;

	/**
	 * Boolean saying if we have captured flag
	 */
	private static boolean				isFlag;

	/**
	 * Lifting speed of the motors to capture the block
	 */
	private static final int			LIFTING_SPEED	= 30;

	/**
	 * Improved Ultrasonic Localizer instance
	 */
	private static ImprovedUsLocalizer	localizer;

	/**
	 * LightSensor polling rate
	 */
	private static final int			LS_WINDOW		= 5;

	/**
	 * Navigation instance
	 */
	private static Navigator			nav;

	/**
	 * Odometer instance
	 */
	private static Odometer				odo;

	/**
	 * Opponent flag type
	 */
	public static int					opponentFlagType;

	/**
	 * X coordinate of opponent home zone
	 */
	public static int					opponentHomeZoneBL_X;

	/**
	 * Y coordinate of opponent home zone
	 */
	public static int					opponentHomeZoneBL_Y;

	/**
	 * UltrasonicSensor polling rate
	 */
	private static final int			US_WINDOW		= 5;

	/**
	 * Ultrasonic sensor instance for the front ultrasonic sensor
	 */
	private static UltrasonicSensor		usFrontSampler;

	/**
	 * Perform the capture of the block
	 */
	public static void capture() {
		Robot.liftingMotor.setAcceleration(15000);

		Robot.leftMotor.rotate(-100, true);
		Robot.rightMotor.rotate(-100, false);
		Delay.msDelay(100);

		nav.turnTo(odo.getOrientation() + Math.PI, true);

		Robot.liftingMotor.setSpeed(LIFTING_SPEED);
		Robot.liftingMotor.rotate(-225);

		nav.goForward(7);
	}

	/*
	 * public static void findFlag() {
	 * double anglePrev = odo.getOrientation();
	 * double angleCurr = odo.getOrientation();
	 * double target = 2 * Math.PI;
	 * int distCurr = 0;
	 * int distPrev = usFrontSampler.getSample()[0];
	 * listOfBlocks = new ArrayList<>();
	 * boolean foundOneBlock = false;
	 * // Third Step: Find at least One Block
	 * while (!foundOneBlock) {
	 * // Rotate robot
	 * Robot.setSpeeds(60, -60);
	 * while (target > 0) {
	 * anglePrev = angleCurr;
	 * angleCurr = odo.getOrientation();
	 * distPrev = distCurr;
	 * distCurr = usFrontSampler.getSample()[0];
	 * // Fourth step: Clock critical points
	 * if (distCurr < 25) {
	 * listBlocks.add(odo.getOrientation());
	 * // Delay until there is no more block
	 * while (usFrontSampler.getSample()[0] < 25)
	 * Delay.msDelay(20);
	 * }
	 * target -= Math.abs(angleCurr - anglePrev);
	 * }
	 * // Stop robot
	 * Robot.setSpeeds(0, 0);
	 * }
	 * }
	 */

	/**
	 * Try to find the flag in 'angle' direction
	 * 
	 * @param angle orientation to look in
	 */
	@SuppressWarnings("boxing")
	public static void findFlag(int angle) {
		int smallestDistance = 100000;
		int index = 0;
		int listDistances[] = new int[6];
		for (int i = 0; i < 6; i++) {
			nav.turnTo(Math.toRadians(angle) - Math.PI / 2, true);
			nav.goForward(5);
			nav.turnTo(Math.toRadians(angle), true);
			Delay.msDelay(2000);
			int distance = usFrontSampler.getSample()[0];
			listDistances[i] = distance;
			if (distance < smallestDistance)
				index = i;
		}
		nav.turnTo(Math.toRadians(angle) + Math.PI, true);
		Delay.msDelay(1000);
		nav.goForward(5 * (5 - index));
		nav.turnTo(angle, true);
		Delay.msDelay(2000);
		int distanceToBlock = usFrontSampler.getSample()[0];
		if (angle == 0) {
			int startX = (int) odo.getPosition().getX();
			while (usFrontSampler.getSample()[0] > 5 && !Capturer.isFlag &&
					(Math.abs(startX - odo.getPosition().getX()) < distanceToBlock + BLOCK_ERROR))
				Robot.setSpeeds(50, 50);
			Robot.setSpeeds(0, 0);
			Delay.msDelay(1000);
			if (Capturer.isFlag) {
				capture();
				lift();
			}
		} else if (angle == 90) {
			int startY = (int) odo.getPosition().getY();
			while (usFrontSampler.getSample()[0] > 5 && !Capturer.isFlag &&
					(Math.abs(startY - odo.getPosition().getY()) < distanceToBlock + BLOCK_ERROR))
				Robot.setSpeeds(50, 50);
			Robot.setSpeeds(0, 0);
			Delay.msDelay(1000);
			if (Capturer.isFlag) {
				capture();
				lift();
			}
		} else if (angle == 180) {
			int startX = (int) odo.getPosition().getX();
			while (usFrontSampler.getSample()[0] > 5 && !Capturer.isFlag &&
					(Math.abs(startX - odo.getPosition().getX()) < distanceToBlock + BLOCK_ERROR))
				Robot.setSpeeds(50, 50);
			Robot.setSpeeds(0, 0);
			Delay.msDelay(1000);
			if (Capturer.isFlag) {
				capture();
				lift();
			}
		}
	}

	/**
	 * Perform lifting of the block
	 */
	public static void lift() {
		while (nav.isNavigating()) {
			Delay.msDelay(100);
		}
		Robot.grabbingMotor.setSpeed(GRABBING_SPEED);
		Robot.grabbingMotor.rotate(90);
		Robot.liftingMotor.rotate(225);
	}

	/**
	 * Lower the claw
	 */
	public static void lower() {
		Robot.liftingMotor.rotate(-225);
		Robot.grabbingMotor.rotate(-90);
		Robot.liftingMotor.rotate(225);
	}

	/**
	 * Entry point of the class
	 * 
	 * @param args
	 */
	@SuppressWarnings({ "nls", "boxing" })
	public static void main(String[] args) {

		// - Initiation step: Get information from server
		// wifiTransmission();
		Sound.beep();

		usFrontSampler = new UltrasonicSensor(Robot.usSensor, true, true, US_WINDOW);

		UltrasonicSensor usAngledSampler = new UltrasonicSensor(Robot.angledUsSensor, true, false, US_WINDOW);
		LightSensor lightSampler = new LightSensor(Robot.lsSensor, true, true, LS_WINDOW);

		odo = new Odometer(true);
		SimpleInterrupter si = new SimpleInterrupter();
		si.setInterrupt(false);
		nav = new Navigator(odo, si);
		finder = new ObjectDetecter(false, 0);

		int button = 3;

		while (button != Button.ID_ESCAPE) {

			// - Display each button's functionality
			Robot.textLCD.clear();
			Robot.textLCD.drawString("Left: Win", 0, 0);
			button = Button.waitForAnyPress();
			Robot.textLCD.clear();
			if (button == Button.ID_LEFT) {

				if (flagType == 0)
					finder.setColorCode(1);
				else
					finder.setColorCode(flagType);

				while (!isFlag) {

					// - First step: Localizing your position

					if (usFrontSampler.getSample()[0] > 30) {
						localizer = new ImprovedUsLocalizer(odo, usFrontSampler, LocalizationType.RISING_EDGE);
						localizer.doLocalization(nav);
					} else {
						localizer = new ImprovedUsLocalizer(odo, usFrontSampler, LocalizationType.FALLING_EDGE);
						localizer.doLocalization(nav);
					}

					Sound.beep();
					// Button.waitForAnyPress();
					lightSampler.setPollingRate(2);
					LightLocalizer lightLocalizer = new LightLocalizer(odo, lightSampler);
					lightLocalizer.doLocalization(nav);
					Sound.beep();
					lightSampler.setPollingRate(5);

					// - Change the coordinates according to the corner we are starting with.
					if (corner != 0) {
						if (corner == 1) {
							// - Coordinates are already good
							continue;
						} else if (corner == 2) {
							Vec2D currentPosition = odo.getPosition();
							odo.setOrientation(odo.getOrientation() + Math.PI / 2);
							odo.setPosition(Vec2D.getVector(300 - currentPosition.getY(), currentPosition.getX()));
						} else if (corner == 3) {
							Vec2D currentPosition = odo.getPosition();
							odo.setOrientation(odo.getOrientation() + Math.PI);
							odo
								.setPosition(Vec2D.getVector(300 - currentPosition.getX(), 300 - currentPosition.getY()));
						} else if (corner == 4) {
							Vec2D currentPosition = odo.getPosition();
							odo.setOrientation(odo.getOrientation() + 3 * Math.PI / 2);
							odo.setPosition(Vec2D.getVector(currentPosition.getY(), 300 - currentPosition.getX()));
						}
					}

					Button.waitForAnyPress();

					avoider = new ObstacleAvoider(odo, si, usAngledSampler, usFrontSampler, false);

					Queue<Vec2D> path = new LinkedList<>();
					// - Corner isn't equal to 0 therefore, we wifi transmitted.
					if (corner != 0) {
						path.offer(Vec2D.getVector(opponentHomeZoneBL_X * 30.48, opponentHomeZoneBL_Y * 30.48));
					}

					// - For testing purposes
					else {
						path.offer(Vec2D.getVector(30.48 * 4, 30.48 * 4));
					}

					avoider.start();
					// - Go to Opponent home zone's bottom left corner.
					while (!path.isEmpty()) {
						if (!si.getInterrupt())
							Navigator.followPath(nav, path, si);
						Delay.msDelay(100);
					}
					avoider.stop();

					Sound.twoBeeps();
					Robot.setSpeeds(0, 0);

					Robot.textLCD.clear();
					Robot.textLCD.drawString("Press any button", 0, 0);
					Robot.textLCD.drawString("To continue", 0, 1);
					button = Button.waitForAnyPress();
					// Start Object detection class
					finder.start();
					findFlag(90);
					if (!Capturer.isFlag) {
						nav.travelTo((opponentHomeZoneBL_X + 1) * 30.48, (opponentHomeZoneBL_Y) * 30.48);
						findFlag(90);
					}
					if (!Capturer.isFlag) {
						nav.travelTo((opponentHomeZoneBL_X + 2) * 30.48, (opponentHomeZoneBL_Y) * 30.48);
						findFlag(180);
					}
					if (!Capturer.isFlag) {
						nav.travelTo((opponentHomeZoneBL_X + 2) * 30.48, (opponentHomeZoneBL_Y + 1) * 30.48);
						findFlag(180);
					}
					if (!Capturer.isFlag) {
						nav.travelTo((opponentHomeZoneBL_X + 2) * 30.48, (opponentHomeZoneBL_Y + 2) * 30.48);
						findFlag(180);
					}
					if (!Capturer.isFlag) {
						nav.travelTo((opponentHomeZoneBL_X + 2) * 30.48, (opponentHomeZoneBL_Y + 2) * 30.48);
						nav.travelTo((opponentHomeZoneBL_X + 2) * 30.48, (opponentHomeZoneBL_Y) * 30.48);
						nav.travelTo((opponentHomeZoneBL_X) * 30.48, (opponentHomeZoneBL_Y) * 30.48);
					}
					if (!Capturer.isFlag) {
						findFlag(0);
					}
					if (!Capturer.isFlag) {
						nav.travelTo((opponentHomeZoneBL_X) * 30.48, (opponentHomeZoneBL_Y + 1) * 30.48);
						findFlag(0);
					}
					if (!Capturer.isFlag) {
						nav.travelTo((opponentHomeZoneBL_X) * 30.48, (opponentHomeZoneBL_Y + 2) * 30.48);
						findFlag(0);
					}
					Delay.msDelay(1000);
					if (Capturer.isFlag) {
						// - Travel to drop-zone
						path = new LinkedList<>();
						if (corner == 0)
							path.offer(Vec2D.getVector(15, 15));
						else
							path.offer(Vec2D.getVector(dropZone_X * 30.48 + 16, dropZone_Y * 30.48 + 16));

						avoider = new ObstacleAvoider(odo, si, usAngledSampler, usFrontSampler, false);
						avoider.start();
						while (!path.isEmpty())
							Navigator.followPath(nav, path, si);

						lower();

						Sound.beepSequenceUp();
					} else
						Sound.beepSequenceUp();
				}
			}
		}
		System.exit(0);
	}

	/**
	 * Function setting the status of the flag finding
	 * 
	 * @param foundBlock foundBlock attribute
	 * @param isFlag isFlag attribute
	 */
	public static void setFound(boolean foundBlock, boolean isFlag) {
		Capturer.isFlag = isFlag;
	}

	/**
	 * Initialize the transmission and set competition parameters
	 */
	public static void wifiTransmission() {
		int[] info = WifiInformation.getInformation();
		corner = info[0];
		homeZoneBL_X = info[1];
		homeZoneBL_Y = info[2];
		opponentHomeZoneBL_X = info[3];
		opponentHomeZoneBL_Y = info[4];
		dropZone_X = info[5];
		dropZone_Y = info[6];
		flagType = info[7];
		opponentFlagType = info[8];
	}
}
