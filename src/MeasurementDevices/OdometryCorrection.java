package MeasurementDevices;

import EV3Hardware.Robot;
import Sensors.LightSensor;
import Utility.Vec2D;
import lejos.utility.Delay;

/**
 * Class performing odometry correction
 */
public class OdometryCorrection extends Thread {
	/**
	 * Error allowed on comparisons
	 */
	private static final float COMPARISON_ERROR = 0.001f;

	/**
	 * The tick rate of the correction logic [ms]
	 */
	private static final long CORRECTION_PERIOD = 5;

	/**
	 * Threshold for detecting a black line
	 */
	private static final float DETECTION_THRESHOLD = 0.18f;

	/**
	 * Instance of the odometer used in this class
	 */
	private Odometer odometer = null;

	/**
	 * Instance of the light sensor poller
	 */
	private LightSensor sensorPoller;

	/**
	 * Constructor taking in an odometer and a LightSensorPoller
	 * 
	 * @param odometer odometer instance
	 * @param sensorPoller instance of a light sensor object
	 */
	public OdometryCorrection(Odometer odometer, LightSensor sensorPoller) {
		assert odometer != null;
		assert sensorPoller != null;
		this.odometer = odometer;
		this.sensorPoller = sensorPoller;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@SuppressWarnings({ "boxing", "nls" })
	@Override
	public void run() {
		long correctionStart, correctionEnd;
		float intensity;
		float prev = 0.f;
		float delta = 0.f;
		Vec2D lastPosition = this.odometer.getPosition();
		double lastOrientation = this.odometer.getOrientation();
		Vec2D currentPosition;
		double currentOrientation;
		boolean firstLine = true;
		boolean firstLineX = true;

		while (true) {
			correctionStart = System.currentTimeMillis();

			intensity = this.sensorPoller.getSample()[0];
			delta = intensity - prev;
			if (Math.abs(delta) > DETECTION_THRESHOLD) {
				// - Just crossed the black line
				// - if delta is + -> Leaving the line
				// - if delta is - -> Entering the line
				if (delta < 0) {
					// Sound.beep();
				} else {
					// Sound.buzz();
					// - Correct with when leaving the value
					// - Get position for the algorithm to correct
					currentPosition = this.odometer.getPosition();
					currentOrientation = this.odometer.getOrientation();
					
					// TODO center value as -range + val, val, +range + val
					// TODO make correction to work at different angles
					// - 0 < t < pi/4 --> Correct Y, +30 cm
					// - pi/4 < t < 3pi/4 --> Correct X, +30 cm
					// - 3pi/4 < t < 5pi/4 --> Correct Y, -30 cm
					// - 5pi/4 < t < 7pi/4 --> Correct X, -30 cm

					if (!firstLine) {
						if (Vec2D.isInRange(currentOrientation, 0, Math.PI / 4,
											COMPARISON_ERROR)) {
							if (Vec2D.isEqual(	currentOrientation,
												lastOrientation,
												COMPARISON_ERROR) &&
								Vec2D.isInRange(Math.abs(currentPosition.getY() -
															lastPosition.getY()),
												29.5, 30.5, COMPARISON_ERROR)) {
								this.odometer.setPosition(
								                          Vec2D.getVector(currentPosition.getX(),
								                                          lastPosition.getY() + 30.));
							}
						} else if (Vec2D.isInRange(	currentOrientation,
													Math.PI / 4,
													3 * Math.PI / 4,
													COMPARISON_ERROR)) {
							if (Vec2D.isEqual(	currentOrientation,
												lastOrientation,
												COMPARISON_ERROR) &&
								Vec2D.isInRange(Math.abs(currentPosition.getX() -
															lastPosition.getX()),
												29.5, 30.5, COMPARISON_ERROR)) {
								this.odometer.setPosition(Vec2D.getVector(lastPosition.getX() + 30.,
								                                          currentPosition.getY()));
							}
						} else if (Vec2D.isInRange(	currentOrientation,
													3 * Math.PI / 4,
													5 * Math.PI / 4,
													COMPARISON_ERROR)) {
							if (Vec2D.isEqual(	currentOrientation,
												lastOrientation,
												COMPARISON_ERROR) &&
								Vec2D.isInRange(Math.abs(currentPosition.getY() -
															lastPosition.getY()),
												29.5, 30.5, COMPARISON_ERROR)) {
								this.odometer.setPosition(Vec2D.getVector(currentPosition.getX(),
								                                          lastPosition.getY() - 30.));
							}
						} else if (Vec2D.isInRange(	currentOrientation,
													5 * Math.PI / 4,
													7 * Math.PI / 4,
													COMPARISON_ERROR)) {
							if (Vec2D.isEqual(	currentOrientation,
												lastOrientation,
												COMPARISON_ERROR) &&
								Vec2D.isInRange(Math.abs(currentPosition.getX() -
															lastPosition.getX()),
												29.5, 30.5, COMPARISON_ERROR)) {
								this.odometer.setPosition(Vec2D.getVector(lastPosition.getX() - 30.,
								                                          currentPosition.getY()));
							}
						}

					} else {
						// - Avoid noise that the sensor gets and correct a bit later
						// System.out.println(currentPosition[Y]);
						// System.out.println(isInRange(currentPosition[Y], 0.5, 1));
						if (Vec2D.isInRange(currentPosition.getY(), 1, 25,
											COMPARISON_ERROR)) {
							// - We now have more than one line so we can correct
							// - on the next iteration
							firstLine = false;

							// - Update the position after the y adjustment
							// - The 4.6 cm is the distance from the sensor
							// - to the wheel-base in y direction
							// TODO change the constant
							this.odometer.setPosition(Vec2D.getVector(	currentPosition.getX(),
																		30.96 + 11.6));
						}
					}

					// - Adjust the first x position
					if (Vec2D.isInRange(currentOrientation, Math.PI / 4,
										3 * Math.PI / 4, COMPARISON_ERROR)) {
						if (firstLineX) {
							// - Detect the first x line only once
							firstLineX = false;

							// - Update the position
							// - 6.0 cm is the distance from the sensor
							// - to the motor in x direction
							// TODO change the constant
							this.odometer.setPosition(Vec2D.getVector(30.96 + 8.0,
							                                          currentPosition.getY()));
						}
					}

					// - Copy the last vales
					lastOrientation = currentOrientation;
					lastPosition = currentPosition.copyOf();
				}
			}

			prev = intensity;

			Robot.textLCD.drawString(String.format("Intensity: %f ", intensity).substring(0, 16), 0, 5);

			// - This ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			long diff_period = correctionEnd - correctionStart;
			if (diff_period < CORRECTION_PERIOD) {
				Delay.msDelay(CORRECTION_PERIOD - diff_period);
			}
		}
	}
}
