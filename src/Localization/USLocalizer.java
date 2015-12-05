package Localization;

import EV3Hardware.Robot;
import MeasurementDevices.Odometer;
import Navigator.Navigator;
import Sensors.UltrasonicSensor;
import lejos.utility.Delay;

/**
 * USLocalizer
 */
public class USLocalizer {
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
	 * 225 Degrees value
	 */
	public static final double ANGLE_225 = 225 * Math.PI / 180;

	/**
	 * 45 Degrees value
	 */
	public static final double ANGLE_45 = 45 * Math.PI / 180;

	/**
	 * Cutoff value [cm] to detection of the wall to space and
	 * vice-versa transition
	 */
	public static final int cutOff = 30;

	/**
	 * Cutoff error [cm]
	 */
	public static final int cutOffError = 3;

	/**
	 * Suggested rotation speed [deg/s]
	 */
	public static int ROTATION_SPEED = 110;

	/**
	 * Error due to the ultrasonic sensor because of time delay [ms]
	 */
	public static final int TIME_ERROR = 2;

	/**
	 * Localization type to use
	 */
	private LocalizationType locType;

	/**
	 * Odometer instance
	 */
	private Odometer odo;

	/**
	 * UltrasonicSensor instance
	 */
	private UltrasonicSensor usSensor;

	/**
	 * @param odo Odometer
	 * @param usSensor UltrasonicSensor instance
	 * @param locType Localization type to use
	 */
	public USLocalizer(	Odometer odo, UltrasonicSensor usSensor,
						LocalizationType locType) {
		assert(odo != null);
		assert(usSensor != null);
		assert(locType != null);

		this.odo = odo;
		this.usSensor = usSensor;
		this.locType = locType;
	}

	/**
	 * Perform UltrasonicSensor Localization
	 * 
	 * @param nav Navigation instance
	 */
	public void doLocalization(Navigator nav) {
		assert(nav != null);
		double angleA, angleB, dTheta;

		if (this.locType == LocalizationType.FALLING_EDGE) {
			// - Rotate the robot until it sees no wall
			while (getFilteredData() < cutOff + cutOffError) {
				Robot.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			}

			// - Wait to avoid getting trash values for the sensor
			Delay.msDelay(1000);

			// - Keep rotating until the robot sees a wall, then latch the angle
			while (getFilteredData() > cutOff + cutOffError) {
				Robot.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			}

			// - Stop the robot rotation
			Robot.setSpeeds(0, 0);

			angleA = this.odo.getOrientation();

			// - Wait to avoid getting trash values for the sensor
			Delay.msDelay(1000);

			// - Switch direction and wait until it sees no wall
			while (getFilteredData() < cutOff + cutOffError) {
				Robot.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			}

			// - Wait to avoid getting trash values for the sensor
			Delay.msDelay(1000);

			// - Keep rotating until the robot sees a wall, then latch the angle
			while (getFilteredData() > cutOff + cutOffError) {
				Robot.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			}

			angleB = this.odo.getOrientation();

			// - Stop the robot rotation
			Robot.setSpeeds(0, 0);

			// - angleA is clockwise from angleB, so assume the average of the
			// - angles to the right of angleB is 45 degrees past 'north'
			if (angleA < angleB)
				dTheta = ANGLE_225 - (angleA + angleB) / 2;
			else
				dTheta = ANGLE_45 - (angleA + angleB) / 2;

			// - Update the odometer position (example to follow:)
			// this.odo.setPosition(Vec2D.getVector(0.0, 0.0));
			this.odo.setOrientation(this.odo.getOrientation() + dTheta);
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

			// - Rotate the robot until it sees a wall
			while (getFilteredData() > cutOff + cutOffError) {
				Robot.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			}

			// - Wait to avoid getting trash values for the sensor
			Delay.msDelay(1500);

			// - Keep rotating until the robot sees no wall, then latch the angle
			while (getFilteredData() < cutOff + cutOffError) {
				Robot.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
			}

			// - Stop the robot rotation
			Robot.setSpeeds(0, 0);

			angleA = this.odo.getOrientation();

			// - Wait to avoid getting trash values for the sensor
			Delay.msDelay(1000);

			// - Switch direction and wait until it sees a wall
			while (getFilteredData() > cutOff + cutOffError) {
				Robot.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			}

			// - Wait to avoid getting trash values for the sensor
			Delay.msDelay(1500);

			// - Keep rotating until the robot sees no wall, then latch the angle
			while (getFilteredData() < cutOff + cutOffError) {
				Robot.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);
			}

			angleB = this.odo.getOrientation();

			// - Stop the robot rotation
			Robot.setSpeeds(0, 0);

			// - angleA is clockwise from angleB, so assume the average of the
			// - angles to the right of angleB is 45 degrees past 'north'
			if (angleA > angleB)
				dTheta = ANGLE_225 - (angleA + angleB) / 2;
			else
				dTheta = ANGLE_45 - (angleA + angleB) / 2;

			// - Update the odometer position (example to follow:)
			// this.odo.setPosition(Vec2D.getVector(0.0, 0.0));
			this.odo.setOrientation(this.odo.getOrientation() + dTheta);

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
	@SuppressWarnings({ "boxing", "nls" })
	private float getFilteredData() {
		int distance = this.usSensor.getSample()[0];

		Robot.textLCD.drawString(String.format(	"Distance: %d [cm]          ",
												distance), 0, 4);

		// - Saturate to 50
		// - UltrasonicSensor class takes care of spurious 255 values
		if (distance > 50)
			distance = 50;

		return distance;
	}
}
