package Localization;

import EV3Hardware.Robot;
import MeasurementDevices.Odometer;
import Navigator.Navigator;
import Sensors.UltrasonicSensor;
import lejos.hardware.Sound;

/**
 * Class performing ultrasonic localization
 */
public class ImprovedUsLocalizer {
	/**
	 * Enumeration describing LocalizationType
	 */
	public enum LocalizationType {

		/**
		 * Falling Edge
		 */
		FALLING_EDGE,

		/**
		 * Rising Edge
		 */
		RISING_EDGE
	}

	/**
	 * Distance read by US sensor
	 */
	private static int dist;

	/**
	 * Noise margin constant
	 */
	private static final int NOISE_MARGIN = 20;

	/**
	 * Suggested rotation speed [deg/s]
	 */
	public static int ROTATION_SPEED = 200;

	/**
	 * Localization type to use
	 */
	private LocalizationType locType;

	/**
	 * The heading we need to add to the odometer's angle in order to get
	 * correct the odometer's theta.
	 */
	private double newHeading;

	/**
	 * Odometer instance
	 */
	private Odometer odo;

	/**
	 * The correct angle that has to be reported by the odometer
	 */
	private double realAngle;

	/**
	 * UltrasonicSensor instance
	 */
	private UltrasonicSensor usSensor;

	/**
	 * @param odo Odometer
	 * @param usSensor UltrasonicSensor instance
	 * @param locType Localization type to use
	 */
	public ImprovedUsLocalizer(	Odometer odo, UltrasonicSensor usSensor,
								LocalizationType locType) {
		assert(odo != null);
		assert(usSensor != null);
		assert(locType != null);

		this.odo = odo;
		this.usSensor = usSensor;
		this.locType = locType;
	}

	/**
	 * Calculates heading
	 * 
	 * @param angleA
	 * @param angleB
	 * @return heading of robot
	 */
	private static double calculateHeading(double angleA, double angleB) {
		double heading;
		if (angleA < angleB)
			heading = (45.0 - (angleA + angleB) / 2.0);
		else
			heading = (225.0 - (angleA + angleB) / 2.0);
		return heading;
	}

	/**
	 * Perform UltrasonicSensor Localization
	 * 
	 * @param nav Navigation instance
	 */
	public void doLocalization(Navigator nav) {
		assert(nav != null);
		double angleA = 0.0, angleB = 0.0;

		// - Booleans that activates and breaks while loop
		boolean fallingEdgeDetected = false;
		boolean risingEdgeDetected = false;

		// - Boolean. If there is no wall, it is true, else it is false
		// - made in order to activate and break while loops
		boolean noWall = false;

		// - Timer used in order to make sure that the robot doesn't detect
		// - the same wall twice.
		long tempTime;

		if (this.locType == LocalizationType.FALLING_EDGE) {
			// - Rotate the robot until it sees no wall in order to stop rotating,
			// - boolean noWall has to return true

			while (!noWall) {
				// - Turn right method
				turnRight();

				// - If no more wall, noWall becomes true in order to break
				// - the while loop
				if (getFilteredData() == 50)
					noWall = true;
			}

			// - Gets current time
			tempTime = System.currentTimeMillis();

			// - Keep rotating until the robot sees a wall, then latch the angle
			// - boolean that breaks the while loop when the falling edge is detected
			while (!fallingEdgeDetected) {
				turnRight();

				// - Falling edge detected is true when US sensor detects a wall
				// - minus the noise_margin in order to make sure it really
				// - detects it.
				// - However, it also has to make sure that the time difference
				// - is over 2000 ms.
				// - This is to make sure that the US sensor doesn't detect the
				// - same wall twice very quickly
				if (getFilteredData() < 50 - NOISE_MARGIN &&
					(System.currentTimeMillis() - tempTime) > 2000) {
					// - Breaks loop
					fallingEdgeDetected = true;

					// - Boolean becomes false in order to turn the other side
					// - and activate the next while loop
					noWall = false;

					// - Latch angle
					angleA = Math.toDegrees(this.odo.getOrientation());
					Sound.beep();
				}
			}

			// - Get time
			tempTime = System.currentTimeMillis();

			// - Switch direction and wait until it sees no wall
			// - The time difference makes sure that it doesn't detect the
			// - same wall twice
			while (!noWall || (System.currentTimeMillis() - tempTime) < 2000) {
				turnLeft();

				// - No more wall boolean turns true to break while loop
				if (getFilteredData() == 50) {
					noWall = true;

					// - Needs to detect another falling edge, hence it's false
					fallingEdgeDetected = false;
				}
			}

			// - Keep rotating until the robot sees a wall, then latch the angle
			while (!fallingEdgeDetected) {
				turnLeft();
				if (getFilteredData() < 50 - NOISE_MARGIN) {
					// - Latch angle
					angleB = Math.toDegrees(this.odo.getOrientation());
					Sound.beep();

					// - Break last while loop
					fallingEdgeDetected = true;
				}
			}
			Robot.setSpeeds(0, 0);

			// - angleA is clockwise from angleB, so assume the average of the
			// - angles to the right of angleB is 45 degrees past 'north'
			// - Calculates heading method with arguments angle a and angle b
			this.newHeading = calculateHeading(angleA, angleB);

			// - The correct angle that the odometer should report
			this.realAngle = this.newHeading +
								Math.toDegrees(this.odo.getOrientation());
			// - Update the odometer position (example to follow:)
			this.odo.setOrientation(Math.toRadians(this.realAngle));

			// - Rotate to 0 degrees
			nav.setRotationSpeed(ROTATION_SPEED);
			nav.turnTo(0, true);

		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */

			noWall = true;
			// - Robot turns until it sees the wall
			while (noWall) {
				turnRight();

				// - If detects wall, no wall becomes false to break while loop
				// - and go to next part
				if (getFilteredData() < 50 - NOISE_MARGIN)
					noWall = false;
			}
			// - Get time
			tempTime = System.currentTimeMillis();

			// - Robot turns until it reaches rising edge.
			// - While loops that continues until it detects rising edge
			while (!risingEdgeDetected) {
				turnRight();

				// - If no more wall, that means rising edge is detected and
				// - make sure that it doesn't detect the same wall twice
				if (getFilteredData() == 50 &&
					System.currentTimeMillis() - tempTime > 2000) {
					risingEdgeDetected = true;

					// - No more wall so true
					noWall = true;

					// - Latch angle b
					angleB = Math.toDegrees(this.odo.getOrientation());
					Sound.beep();
				}
			}

			// - Get time
			tempTime = System.currentTimeMillis();

			// - Robot switches direction until it sees the wall
			while (noWall) {
				turnLeft();

				// - Time difference makes sure that it doesn't detect the same
				// - wall twice very quickly
				if (getFilteredData() < 50 - NOISE_MARGIN &&
					(System.currentTimeMillis() - tempTime) > 2000) {
					// - Rising edge becomes false because we need to detect a
					// - second one for angleA
					noWall = false;
					risingEdgeDetected = false;
				}
			}
			// - Robot rotates until it doesn't see the wall
			// - Stops when rising edge is detected
			while (!risingEdgeDetected) {
				turnLeft();
				if (getFilteredData() == 50) {
					// - Latches angle when there is no more wall
					angleA = Math.toDegrees(this.odo.getOrientation());

					// - Break while loop
					risingEdgeDetected = true;
					noWall = true;
					Sound.beep();
				}
			}
			Robot.setSpeeds(0, 0);

			// - Calculate heading to add to current
			this.newHeading = calculateHeading(angleA, angleB);
			// - Correct angle to be reported by the odomter
			this.realAngle = this.newHeading +
								Math.toDegrees(this.odo.getOrientation());

			// - Update the odometer position (example to follow:)
			// this.odo.setPosition(Vec2D.getVector(0.0, 0.0));
			this.odo.setOrientation(Math.toRadians(this.realAngle));

			// - Rotate to 0 degrees
			nav.setRotationSpeed(ROTATION_SPEED);
			nav.turnTo(0, true);
		}
	}

	/**
	 * Filter the sample
	 * 
	 * @return Filtered sample
	 */
	@SuppressWarnings({ "boxing" })
	private float getFilteredData() {
		int distance = this.usSensor.getSample()[0];

		// - Filter, if distance bigger than 50, return simply 50
		if (distance >= 50)
			dist = 50;
		// - Else returns the actual distance.
		else
			dist = distance;
		return ImprovedUsLocalizer.dist;
	}

	/**
	 * turn left method
	 */
	private static void turnLeft() {
		Robot.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
	}

	/**
	 * turn right method
	 */
	private static void turnRight() {
		Robot.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
	}
}
