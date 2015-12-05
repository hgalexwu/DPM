package MeasurementDevices;

import EV3Hardware.Robot;
import Utility.TimerInterface;
import Utility.Vec2D;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * Class performing odometry on the robot
 */
public class Odometer implements TimerListener, TimerInterface {

	/**
	 * Suggested tick rate for the odometer as an interval in [ms].
	 */
	public static final int TICK_RATE = 5;

	/**
	 * Variable storing the vehicle displacement
	 */
	private double dDisplacement = 0.0;

	/**
	 * Variable storing the change in heading
	 */
	private double dHeading = 0.0;

	/**
	 * Variable storing the wheel displacements of the robot
	 */
	private Vec2D distance = Vec2D.getNull();

	/**
	 * Variable storing the position deltas of the robot
	 */
	private Vec2D dPosition = Vec2D.getNull();

	/**
	 * Variable storing the last tachometer value
	 */
	private Vec2D lastTacho = Vec2D.getNull();

	/**
	 * Lock object for mutual exclusion
	 */
	private final Object lock = new Object();

	/**
	 * Variable storing the current tachometer value
	 */
	private Vec2D nowTacho = Vec2D.getNull();

	/**
	 * Timer for automatic polling
	 */
	private Timer odoTimer;

	/**
	 * Variable storing the orientation of the robot
	 */
	private double orientation = Math.PI / 2;

	/**
	 * Variable storing the position of the robot
	 */
	private Vec2D position = Vec2D.getNull();

	/**
	 * A default constructor
	 * 
	 * @param autostart Start polling automatically or not
	 */
	public Odometer(boolean autostart) {
		// - Create a timer
		this.odoTimer = new Timer(Odometer.TICK_RATE, this);
		if (autostart)
			this.odoTimer.start();
	}

	/**
	 * Getter for the orientation of the robot
	 * 
	 * @return scalar representing robot's orientation
	 */
	public double getOrientation() {
		synchronized (this.lock) {
			return this.orientation;
		}
	}

	/**
	 * Getter for the position of the robot
	 * 
	 * @return vector representing robot's position
	 */
	public Vec2D getPosition() {
		assert(this.position != null);

		synchronized (this.lock) {
			return this.position.copyOf();
		}
	}

	/**
	 * Setter for the orientation of the robot
	 * 
	 * @param orientation new robot's orientation
	 */
	public void setOrientation(double orientation) {
		synchronized (this.lock) {
			this.orientation = orientation;
		}
	}

	/**
	 * Setter for the position of the robot
	 * 
	 * @param position new robot's position
	 */
	public void setPosition(Vec2D position) {
		assert(position != null);
		synchronized (this.lock) {
			this.position.setVector(position);
		}
	}

	/**
	 * @see Utility.TimerInterface#start()
	 */
	@Override
	public void start() {
		this.odoTimer.start();
	}

	/**
	 * @see Utility.TimerInterface#stop()
	 */
	@Override
	public void stop() {
		this.odoTimer.stop();
	}

	/**
	 * Updates the odometer
	 */
	@Override
	public void timedOut() {
		// - Get tachometer counts
		this.nowTacho = Robot.getTachoCount();

		// - Set the distance of the vector, this code is not really nicely
		// - formatted. The part on the right side indicate the scale factors
		// - for the <x, y> components
		this.distance.setVector(Vec2D.sub(this.nowTacho, this.lastTacho).scale(
																				(Math.PI *
																					Robot.LWHEEL_RADIUS /
																					180),
																				(Math.PI *
																					Robot.RWHEEL_RADIUS /
																					180)));
		// - Save tachometer counts for next iteration
		this.lastTacho.setVector(this.nowTacho);

		// - Compute vehicle displacement
		this.dDisplacement = this.distance.componentSum() / 2;
		// - Compute change in heading
		this.dHeading = -this.distance.componentDiff() / Robot.TRACK;

		synchronized (this.lock) {
			// - Update heading
			this.orientation += this.dHeading * Robot.ANGLE_SCALE;
			// - Fix the angle
			this.orientation = Vec2D.fixAngle(this.orientation);
			// - Compute displacement
			this.dPosition.setMagAngle(this.dDisplacement, this.orientation);
			// - Update estimates the position
			this.position.add(this.dPosition);
		}
	}
}
