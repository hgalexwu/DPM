package Localization;

import EV3Hardware.Robot;
import MeasurementDevices.Odometer;
import Navigator.Navigator;
import Sensors.LightSensor;
import Utility.Vec2D;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Delay;

/**
 * LightLocalizer
 */
public class LightLocalizer {

	/**
	 * Cutoff value [intensity] of the grid detector
	 */
	public static final double cutDiff = 0.08;

	/**
	 * Cutoff error [intensity]
	 */
	public static final double cutOffError = 0.01;

	/**
	 * Suggested rotation speed [deg/s]
	 */
	public static int ROTATION_SPEED = 160;

	/**
	 * Instance of a light sensor
	 */
	private LightSensor ls;

	/**
	 * Odometer instance
	 */
	private Odometer odo;

	/**
	 * @param odo odometer
	 * @param pLS instance of the light sensor
	 */
	public LightLocalizer(Odometer odo, LightSensor pLS) {
		assert(odo != null);
		assert(pLS != null);

		this.odo = odo;
		this.ls = pLS;
	}

	/**
	 * Performs localization
	 * 
	 * @param nav Navigation instance
	 */
	@SuppressWarnings({ "boxing" })
	public void doLocalization(Navigator nav) {
		assert(nav != null);
		// - Stop the robot rotation
		Robot.setSpeeds(0, 0);

		// nav.travelTo(11, 11);

		// - Drive to location listed in tutorial
		// nav.setForwardSpeed(ROTATION_SPEED);
		nav.setRotationSpeed(ROTATION_SPEED);
		nav.turnTo(Math.PI / 4, true);
		Robot.rotateDistanceLocalization(275, 275);

		// - Prepare values for the differential detection of grid lines
		float intensity = 0.f;
		float prev = 0.f;
		float delta = 0.f;

		// - Number of detected grid lines
		int gridlines = 0;
		double[] angles = new double[4];

		prev = this.ls.getSample()[0];

		// - A crime against humanity
		long correctionStart, correctionEnd;

		// - Start rotating and clock all 4 grid-lines
		while ((gridlines < 4) && (Button.readButtons() != Button.ID_ESCAPE)) {
			correctionStart = System.currentTimeMillis();

			Robot.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);

			intensity = this.ls.getSample()[0];
			delta = intensity - prev;

			// - Show the value of intensity
			// Robot.textLCD.drawString(String.format("Intensity: %.2f ", intensity), 0, 5);
			// Robot.textLCD.drawString(String.format("Delta: %.2f ", delta), 0, 6);
			if (Math.abs(delta) > cutDiff + cutOffError) {
				// Just crossed the black line
				// if delta is + -> Leaving the line
				// - -> Entering the line
				if (delta > 0) {
					// Sound.beep();
					angles[gridlines++] = this.odo.getOrientation();
				} else {
					assert(true);
				}
			}

			prev = intensity;
			// TODO Remove this awfulness
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < 115) {
				Delay.msDelay(115 - (correctionEnd - correctionStart));
			}
		}
		Sound.beep();
		// - Do trigonometry to compute (0,0) and 0 degrees
		double xtheta = Math.abs(angles[0] - angles[2]);
		double ytheta = Math.abs(angles[1] - angles[3]);
		double x = -Robot.LS_TO_ROTATION_CENTER * Math.cos(ytheta / 2);
		double y = -Robot.LS_TO_ROTATION_CENTER * Math.cos(xtheta / 2);
		// double dTheta = Math.PI - xtheta - ytheta / 2 - ytheta / 2 + Math.abs(2 * Math.PI - odo.getOrientation());
		// double dTheta = -Math.PI + xtheta + ytheta;
		// double dTheta = Math.PI / 2 + angles[1] - angles[0];

		this.odo.setPosition(Vec2D.getVector(x, y));
		// this.odo.setOrientation(this.odo.getOrientation() + dTheta);

		// - When done travel to (0,0) and turn to 0 degrees
		nav.travelTo(0, 0);

		// - Stop navigation code
		nav.turnTo(0, true);

	}
}
