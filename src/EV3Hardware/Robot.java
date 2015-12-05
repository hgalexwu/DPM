package EV3Hardware;

import Utility.Vec2D;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

/**
 * Class for accessing static Robot resources
 */
public class Robot {

	/**
	 * Actual value to get a physical rotation of 90 degrees.
	 * This value is used to correct the odometer's imprecision.
	 */
	public static final double ANGLE_90_DEG = 90;

	/**
	 * Scaling factor correcting the angle of rotation
	 */
	public static final double ANGLE_SCALE = 90.0 / ANGLE_90_DEG;

	/**
	 * EV3UltrasonicSensor instance for the angled usSensor
	 */
	@SuppressWarnings("nls")
	public static final EV3UltrasonicSensor angledUsSensor = new EV3UltrasonicSensor(LocalEV3
																								.get()
																								.getPort("S4"));

	/**
	 * EV3ColorSensor instance for the block detection
	 */
	@SuppressWarnings("nls")
	public static final EV3ColorSensor blockSensor = new EV3ColorSensor(LocalEV3
																				.get()
																				.getPort("S3"));

	/**
	 * EV3MediumRegulatedMotor instance for grabbing the block and operating the
	 * claw
	 */
	@SuppressWarnings("nls")
	public static final EV3MediumRegulatedMotor grabbingMotor = new EV3MediumRegulatedMotor(LocalEV3
																									.get()
																									.getPort("C"));

	/**
	 * Instance of the left motor of the EV3 Brick assumed to be connected to
	 * the port "A"
	 */
	@SuppressWarnings("nls")
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3
																								.get()
																								.getPort("A"));

	/**
	 * EV3LargeRegulatedMotor instance responsible for lifting the block upwards
	 * or downwards
	 */
	@SuppressWarnings("nls")
	public static final EV3LargeRegulatedMotor liftingMotor = new EV3LargeRegulatedMotor(LocalEV3
																									.get()
																									.getPort("B"));

	/**
	 * Distance from the center of rotation to the sensor [cm]
	 */
	public static final double LS_TO_ROTATION_CENTER = 11.75;

	/**
	 * Light sensor port instance
	 */
	@SuppressWarnings("nls")
	public static final Port lsPort = LocalEV3.get().getPort("S1");

	/**
	 * EV3ColorSensor instance for red mode which is used for light localization
	 * and grid detection
	 */
	public static final EV3ColorSensor lsSensor = new EV3ColorSensor(lsPort);

	/**
	 * Radius of the left wheel [cm]
	 */
	public static final double LWHEEL_RADIUS = 2.1;

	/**
	 * Instance of the right motor of the EV3 Brick assumed to be connected to
	 * the port "D"
	 */
	@SuppressWarnings("nls")
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3
																								.get()
																								.getPort("D"));

	/**
	 * Radius of the right wheel
	 */
	public static final double RWHEEL_RADIUS = 2.1;

	/**
	 * Instance of the EV3 display
	 */
	public static final TextLCD textLCD = LocalEV3.get().getTextLCD();

	/**
	 * Length of the track (distance between centers of left and right wheels)
	 */
	public static final double	TRACK	= 16.8;
	/**
	 * Ultrasonic sensor port for the lower leveled ultrasonic sensor
	 */
	@SuppressWarnings("nls")
	public static final Port	usPort	= LocalEV3.get().getPort("S2");

	/**
	 * EV3UltrasonicSensor instance for polling data from the lower leveled
	 * usSensor
	 */
	public static final EV3UltrasonicSensor usSensor = new EV3UltrasonicSensor(usPort);

	/**
	 * Converts angles into rotations of wheel
	 * 
	 * @param angle Angle to turn the robot
	 * @return distance Rotations of wheel to turn a specific angle
	 */
	public static int convertAngle(double angle) {
		return convertDistance(Math.PI * TRACK * angle / 360.0);
	}

	/**
	 * Converts distances into rotations of wheel
	 * 
	 * @param distance distance [cm]
	 * @return Rotations of wheel to go forward a specific distance
	 */
	public static int convertDistance(double distance) {
		return (int) ((180.0 * distance) / (Math.PI * LWHEEL_RADIUS));
	}

	/**
	 * Obtain the tachometer values of the motors
	 * 
	 * @return Vector having the following contents <tacho.left, tacho.right>
	 */
	public static synchronized Vec2D getTachoCount() {
		return Vec2D.getVector(	leftMotor.getTachoCount(),
								rightMotor.getTachoCount());
	}

	/**
	 * Rotate method for the square driver to turn 90 right
	 * 
	 * @param lSpeed Left wheel's speed
	 * @param rSpeed Right wheel's speed
	 */
	public static void rotateAngle(int lSpeed, int rSpeed) {
		Robot.leftMotor.setSpeed(lSpeed);
		Robot.rightMotor.setSpeed(rSpeed);
		Robot.leftMotor.rotate(convertAngle(45), true);
		Robot.rightMotor.rotate(-convertAngle(45), false);
	}

	/**
	 * Rotate method for the square driver to go forward
	 * 
	 * @param lSpeed Left wheel's speed
	 * @param rSpeed Right wheel's speed
	 */
	public static void rotateDistance(int lSpeed, int rSpeed) {
		Robot.leftMotor.setSpeed(lSpeed);
		Robot.rightMotor.setSpeed(rSpeed);
		Robot.leftMotor.rotate(convertDistance(60.96), true);
		Robot.rightMotor.rotate(convertDistance(60.96), false);
	}

	/**
	 * Rotate method for the localization to go approximately to origin.
	 * 
	 * @param lSpeed Left wheel's speed
	 * @param rSpeed Right wheel's speed
	 */
	public static void rotateDistanceLocalization(int lSpeed, int rSpeed) {
		Robot.leftMotor.setSpeed(lSpeed);
		Robot.rightMotor.setSpeed(rSpeed);
		Robot.leftMotor.rotate(convertDistance(15), true);
		Robot.rightMotor.rotate(convertDistance(15), false);
	}

	/**
	 * Changes the speed of the motors taking into account the sign
	 * 
	 * @param lSpeed speed of the left motor
	 * @param rSpeed speed of the right motor
	 */
	public static void setSpeeds(int lSpeed, int rSpeed) {
		Robot.leftMotor.setSpeed(lSpeed);
		Robot.rightMotor.setSpeed(rSpeed);
		if (lSpeed < 0.0)
			Robot.leftMotor.backward();
		else
			Robot.leftMotor.forward();
		if (rSpeed < 0.0)
			Robot.rightMotor.backward();
		else
			Robot.rightMotor.forward();
	}
}
