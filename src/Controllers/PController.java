package Controllers;

import EV3Hardware.Robot;
import Sensors.UltrasonicSensor;
import Utility.TimerInterface;
import lejos.utility.Delay;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * This class provides an implementation of the wall follower
 */
public class PController implements TimerListener, TimerInterface {

	/**
	 * Critical time [ms] is the time to suspend the robot is avoid a
	 * very close obstacle
	 */
	public static final int CRITICAL_TIME = 750;

	/**
	 * Suggested tick rate [ms] for the P-Controller
	 */
	public static final int TICK_RATE = 20;

	/**
	 * Distance to maintain from the wall to the sensor
	 */
	private final int bandCenter;

	/**
	 * Error margin for activation of the correction algorithm
	 */
	private final int bandWidth;

	/**
	 * Distance from the wall to the robot
	 */
	private int distance;

	/**
	 * Flag isPControlling to avoid overhead cause by start/stop method of the
	 * timer class
	 */
	private boolean isPControlling = false;

	/**
	 * Maximum amount of correction
	 */
	private final int maxCorrection = 60;

	/**
	 * Maximum distance from the wall
	 */
	private final int maxDist = 250;

	/**
	 * Forward speed of the motors
	 */
	private final int motorStraight = 200;

	/**
	 * Constant proportional to the error
	 */
	private final float propConst = 9.5f;

	/**
	 * Timer for automatic polling
	 */
	private Timer pTimer;

	/**
	 * Instance of the UltraSonic sensor
	 */
	private UltrasonicSensor us = null;

	/**
	 * Basic constructor for the P-Controller
	 * 
	 * @param bandCenter Distance to maintain from the wall to the sensor
	 * @param bandwidth Error margin for activation of the correction algorithm
	 * @param us Instance of fully constructed UltrasonicSensor
	 */
	public PController(int bandCenter, int bandwidth, UltrasonicSensor us) {
		assert(us != null);

		this.bandCenter = bandCenter;
		this.bandWidth = bandwidth;
		this.us = us;

		// - Create a timer
		this.pTimer = new Timer(PController.TICK_RATE, this);
		this.pTimer.start();
	}

	/**
	 * Calculates the amount of correction proportional to the error
	 * 
	 * @param diff error
	 * @return Amount of proportional correction
	 */
	private int calcProp(int diff) {
		// - Compute the correction factor
		int correction = (int) (this.propConst * (double) Math.abs(diff));
		// - Apply correction limit if needed
		if (correction >= this.motorStraight)
			correction = this.maxCorrection;

		return correction;
	}

	/**
	 * Returns the state of the P-Controller.
	 * 
	 * @return True is P-Controlling is engaged and false otherwise
	 */
	public boolean getPControllerNavigating() {
		return this.isPControlling;
	}

	/**
	 * @see Utility.TimerInterface#start()
	 */
	@Override
	public void start() {
		this.isPControlling = true;
	}

	/**
	 * @see Utility.TimerInterface#stop()
	 */
	@Override
	public void stop() {
		this.isPControlling = false;
	}

	/**
	 * Performs a cycle of P-Controller operation
	 */
	@SuppressWarnings("boxing")
	@Override
	public void timedOut() {
		if (!this.isPControlling)
			return;

		if (this.us.getSample() != null)
			this.distance = this.us.getSample()[0];
		int diff;
		int distError = 0;

		// - Compute error term
		if (this.distance <= this.maxDist)
			distError = this.bandCenter - this.distance;

		// - P-Controller Logic
		// - When the robot is too close, turn it in the clockwise direction
		if (this.distance < 4 * this.bandWidth) {
			Robot.setSpeeds(this.motorStraight / 2, -this.motorStraight / 2);
			Delay.msDelay(CRITICAL_TIME);
		}

		// - Case 1: Error in bounds, no correction
		else if (Math.abs(distError) <= this.bandWidth) {
			Robot.setSpeeds(this.motorStraight, this.motorStraight);
		}

		// - Case 2: positive error, move away from wall
		else if (distError > 0) {
			// Get correction value and apply
			diff = calcProp(distError);
			Robot.setSpeeds(this.motorStraight +diff,
							this.motorStraight - diff);
		}

		// - Case 3: negative error, move towards wall
		else if (distError < 0) {
			// Get correction value and apply
			diff = calcProp(distError);
			Robot.setSpeeds(this.motorStraight -diff,
							this.motorStraight + diff);
		}
	}
}
