package FlagCapturer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Delay;
import EV3Hardware.Robot;
import Localization.USLocalizer;
import Localization.USLocalizer.LocalizationType;
import MeasurementDevices.Odometer;
import Navigator.Navigator;
import Navigator.ObstacleAvoider;
import ObjectLocalization.ObjectDetecter;
import Sensors.LightSensor;
import Sensors.UltrasonicSensor;
import Utility.SimpleInterrupter;
import Utility.Vec2D;

/**
 * Class implementing the task of the robot
 */
public class CaptureTheFlag {
	/**
	 * ObstacleAvoider instance
	 */
	private static ObstacleAvoider	avoider;

	/**
	 * Distance from the block [cm]
	 */
	private final static int		BLOCK_DIST_CONSTANT	= 30;

	/**
	 * Error on the distance from the block [cm]
	 */
	private final static int		BLOCK_ERROR			= 2;

	/**
	 * Constant indicating the starting corner
	 */
	// public static StartCorner corner;

	/**
	 * Deploy zone X coordinate
	 */
	public static int				dropZone_X;

	/**
	 * Deploy zone Y coordinate
	 */
	public static int				dropZone_Y;

	/**
	 * Object Finder instance
	 */
	private static ObjectDetecter	finder;

	/**
	 * Target type
	 */
	public static int				flagType;

	/**
	 * Flag indicating whether the block was found or not
	 */
	private static boolean			foundBlock;

	/**
	 * Speed at which the block is grabbed in [deg/s]
	 */
	private static final int		GRABBING_SPEED		= 25;

	/**
	 * Home zone X coordinate
	 */
	public static int				homeZoneBL_X;

	/**
	 * Home zone Y coordinate
	 */
	public static int				homeZoneBL_Y;

	/**
	 * TODO
	 */
	private static boolean			isFlag;

	/**
	 * Speed at which the block is lifted [deg/s]
	 */
	private static final int		LIFTING_SPEED		= 15;

	/**
	 * Ultrasonic Localizer instance
	 */
	private static USLocalizer		localizer;

	/**
	 * LightSensor polling rate [ms]
	 */
	private static final int		LS_RATE				= 3;

	/**
	 * Navigation instance
	 */
	private static Navigator		nav;

	/**
	 * Odometer instance
	 */
	private static Odometer			odo;

	/**
	 * Flag type of the opponent
	 */
	public static int				opponentFlagType;

	/**
	 * Opponent home zone X coordinate
	 */
	public static int				opponentHomeZoneBL_X;

	/**
	 * Opponent home zone Y coordinate
	 */
	public static int				opponentHomeZoneBL_Y;

	/**
	 * UltrasonicSensor polling rate [ms]
	 */
	private static final int		US_RATE				= 5;

	/**
	 * Function performing the capture of the block
	 */
	public static void capture() {

		nav.turnTo(odo.getOrientation() + Math.PI, true);

		while (nav.isNavigating())
			Delay.msDelay(1000);

		Robot.liftingMotor.setSpeed(LIFTING_SPEED);
		Robot.liftingMotor.rotate(-225);

		nav.goForward(10);

		Sound.beep();

		while (nav.isNavigating())
			Delay.msDelay(1000);

		Robot.grabbingMotor.setSpeed(GRABBING_SPEED);
		Robot.grabbingMotor.rotate(90);
		Robot.liftingMotor.rotate(225);

		Delay.msDelay(1000);

		Robot.liftingMotor.rotate(-225);
		Robot.grabbingMotor.rotate(-90);
		Robot.liftingMotor.rotate(225);
	}

	/**
	 * Entry point of the program
	 * 
	 * @param args
	 */
	@SuppressWarnings({ "unused", "nls", "boxing" })
	public static void main(String[] args) {

		/*
		 * WifiConnection conn = null;
		 * try {
		 * conn = new WifiConnection(SERVER_IP, TEAM_NUMBER);
		 * } catch (IOException e) {
		 * LCD.drawString("Connection failed", 0, 8);
		 * }
		 * // example usage of Transmission class
		 * Transmission t = conn.getTransmission();
		 * if (t == null) {
		 * LCD.drawString("Failed to read transmission", 0, 5);
		 * } else {
		 * corner = t.startingCorner;
		 * homeZoneBL_X = t.homeZoneBL_X;
		 * homeZoneBL_Y = t.homeZoneBL_Y;
		 * opponentHomeZoneBL_X = t.opponentHomeZoneBL_X;
		 * opponentHomeZoneBL_Y = t.opponentHomeZoneBL_Y;
		 * dropZone_X = t.dropZone_X;
		 * dropZone_Y = t.dropZone_Y;
		 * flagType = t.flagType;
		 * opponentFlagType = t.opponentFlagType;
		 * // print out the transmission information
		 * conn.printTransmission();
		 * }
		 */

		UltrasonicSensor usFrontSampler = new UltrasonicSensor(Robot.usSensor, true, false, US_RATE);
		UltrasonicSensor usAngledSampler = new UltrasonicSensor(Robot.angledUsSensor, true, false, US_RATE);
		new LightSensor(Robot.lsSensor, true, false, LS_RATE);

		odo = new Odometer(true);
		nav = new Navigator(odo);
		finder = new ObjectDetecter(false, 0);

		SimpleInterrupter si = new SimpleInterrupter();
		si.setInterrupt(false);
		avoider = new ObstacleAvoider(odo, si, usAngledSampler, usFrontSampler, false);
		foundBlock = false;
		isFlag = false;
		// - Set color code of the block
		finder.setColorCode(1);

		double[] coordinates = new double[] { 5 * 30.48, 5 * 30.48 };

		// - Display each button's functionality
		Robot.textLCD.clear();
		Robot.textLCD.drawString("Left: Start", 0, 0);
		int button = 3;

		while (button != Button.ID_ESCAPE) {
			button = Button.waitForAnyPress();
			Robot.textLCD.clear();

			if (button == Button.ID_LEFT) {
				// int startTime = (int) System.currentTimeMillis();
				// while (((System.currentTimeMillis() - startTime) < COMPETITION_TIME) && !isFlag) {
				while (!isFlag) {
					// - First step: Localizing your position
					localizer = new USLocalizer(odo, usFrontSampler, LocalizationType.RISING_EDGE);
					localizer.doLocalization(nav);

					Sound.twoBeeps();

					Button.waitForAnyPress();
					finder.start();

					while (!isFlag) {
						Delay.msDelay(1000);
					}

					capture();

					// - Second step: Navigating to capture the flag area
					avoider.start();

					Queue<Vec2D> path = new LinkedList<>();
					path.offer(Vec2D.getVector(coordinates[0], coordinates[1]));
					Navigator.followPath(nav, path, si);
					// - Finish navigation before doing pattern recognition
					// while (nav.isNavigating()) {
					// Delay.msDelay(10000);
					// }
					while (!path.isEmpty()) {
						Navigator.followPath(nav, path, si);
					}

					avoider.stop();
					// - Make sure there is no block in front when it starts to turn
					while (usFrontSampler.getSample()[0] < 30) {
						Robot.setSpeeds(50, -50);
						Delay.msDelay(100);
					}

					double anglePrev = odo.getOrientation();
					double angleCurr = odo.getOrientation();
					double target = 2 * Math.PI;
					int distCurr = 0;
					int distPrev = usFrontSampler.getSample()[0];

					ArrayList<Double[]> listOfBlocks = new ArrayList<>();
					boolean foundOneBlock = false;

					// - Third Step: Find at least One Block
					while (!foundOneBlock) {
						// - Rotate robot
						Robot.setSpeeds(50, -50);
						while (target > 0) {
							anglePrev = angleCurr;
							angleCurr = odo.getOrientation();
							distPrev = distCurr;
							distCurr = usFrontSampler.getSample()[0];
							// - Fourth step: Clock critical points
							if (Vec2D.isInRange(distCurr, 5, BLOCK_DIST_CONSTANT, BLOCK_ERROR) &&
								Vec2D.isInRange(distPrev, BLOCK_DIST_CONSTANT, BLOCK_DIST_CONSTANT + 10, BLOCK_ERROR)) {
								// if (distCurr < BLOCK_DIST_CONSTANT && distPrev > BLOCK_DIST_CONSTANT) {
								Sound.beep();
								listOfBlocks.add(new Double[] { odo.getPosition().getX(),
												odo.getPosition().getY(),
												odo.getOrientation(),
												(double) System.currentTimeMillis(),
												(double) distCurr });

							} else if (distCurr - distPrev > 0 &&
										Vec2D.isEqual(distCurr - distPrev, distCurr - distPrev, BLOCK_ERROR)) {
								Sound.beep();
								listOfBlocks.add(new Double[] { odo.getPosition().getX(),
												odo.getPosition().getY(),
												odo.getOrientation(),
												(double) System.currentTimeMillis(),
												(double) distCurr });
								// } else if (distCurr > BLOCK_DIST_CONSTANT && distPrev < BLOCK_DIST_CONSTANT) {
							} else if (Vec2D.isInRange(distPrev, 5, BLOCK_DIST_CONSTANT, BLOCK_ERROR) &&
										Vec2D.isInRange(distCurr, BLOCK_DIST_CONSTANT, BLOCK_DIST_CONSTANT + 10,
														BLOCK_ERROR)) {
								Sound.beep();
								listOfBlocks.add(new Double[] { odo.getPosition().getX(),
												odo.getPosition().getY(),
												odo.getOrientation(),
												(double) System.currentTimeMillis(),
												(double) distCurr });
							}
							target -= Math.abs(angleCurr - anglePrev);
						}
						// - Stop robot
						Robot.setSpeeds(0, 0);
						for (int i = 0; i < listOfBlocks.size(); i += 3) {
							// - Make sure that the two edges are captured within 3 seconds
							if (listOfBlocks.size() - i >= 3 &&
								Math.abs(listOfBlocks.get(i + 2)[3] - listOfBlocks.get(i)[3]) < 3000) {
								Sound.beep();
								finder.start();
								foundOneBlock = true;
								double orientation = (listOfBlocks.get(i + 2)[2] - listOfBlocks.get(i)[2]) / 2;

								// - Turn to first object
								nav.turnTo(listOfBlocks.get(i + 2)[2] - orientation, true);
								nav.goForward(listOfBlocks.get(i + 2)[4] * Math.tan(orientation));

								while (nav.isNavigating()) {
									Delay.msDelay(10000);
								}

								// - Make sure robot reaches the block
								while (!foundBlock && usFrontSampler.getSample()[0] > 4)
									Robot.setSpeeds(30, 30);

								Robot.setSpeeds(0, 0);

								while (!foundBlock) {
									Delay.msDelay(1000);
								}

								if (isFlag) {
									capture();
									break;
								}
							}
							if (listOfBlocks.size() - (i + 3) < 0)
								break;
						}
					}
					finder.stop();
					avoider.stop();
				}
			} else
				System.exit(0);
		}
	}

	/**
	 * Function setting the status of the flag finding
	 * 
	 * @param foundBlock foundBlock attribute
	 * @param isFlag isFlag attribute
	 */
	public static void setFound(boolean foundBlock, boolean isFlag) {
		CaptureTheFlag.foundBlock = foundBlock;
		CaptureTheFlag.isFlag = isFlag;
	}
}
