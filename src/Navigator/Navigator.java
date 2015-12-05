package Navigator;

import java.util.Queue;

import EV3Hardware.Robot;
import MeasurementDevices.Odometer;
import Utility.SimpleInterrupter;
import Utility.Vec2D;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * A controller allowing the navigation of the robot
 */
public class Navigator {

	/**
	 * Maximum angle error
	 */
	private static final double ANGLE_ERROR = 3.0 * Math.PI / 180.0;

	/**
	 * Maximum error for the position
	 */
	private static final double POSITION_ERROR = 1.0;

	/**
	 * This static function makes robot to follow the path until an interruption signal is sent
	 * 
	 * @param nav Instance of the navigator
	 * @param qw Way-points that the robot to go though.
	 *            Once a way-point is archived it is removed from the queue
	 * @param si Interrupter instance that will be externally set to indicate
	 *            that we should stop following the path
	 */
	public static void followPath(	Navigator nav, Queue<Vec2D> qw,
									SimpleInterrupter si) {
		assert(nav != null);
		assert(qw != null);

		// - Travel while we have way-point and we are allowed to proceed
		while (!qw.isEmpty() && !si.getInterrupt()) {
			assert(qw.peek() != null);

			// - Set point and start traveling
			nav.travelTo(qw.peek());

			// - If we reached destination then proceed to the next point
			if (!nav.isNavigating())
				qw.poll();
		}
	}

	/**
	 * Forward speed of the robot
	 */
	private int FORWARD_SPEED = 200;

	/**
	 * Lock object for mutual exclusion
	 */
	private final Object lock = new Object();

	/**
	 * Odometer instance used for navigation
	 */
	private Odometer odometer = null;

	/**
	 * Position of the robot
	 */
	private Vec2D pos = Vec2D.getNull();

	/**
	 * Rotation speed of the robot
	 */
	private int ROTATE_SPEED = 200;

	/**
	 * The simple interrupter instance
	 */
	private SimpleInterrupter si = null;

	/**
	 * Target of the Navigation
	 */
	private Vec2D target = Vec2D.getNull();

	/**
	 * Constructor taking in the odometer as a parameter
	 * 
	 * @param odometer odometer instance
	 * @param autostart Start polling automatically or not
	 */
	public Navigator(Odometer odometer) {
		assert(odometer != null);
		this.odometer = odometer;

		// - Reset the motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {	Robot.leftMotor,
																			Robot.rightMotor }) {
			motor.stop();
			motor.setAcceleration(5000);
		}
	}

	/**
	 * Constructor taking in the odometer as a parameter
	 * 
	 * @param odometer odometer instance
	 * @param si An instance of the SimpleInterrupter
	 * @param autostart Start polling automatically or not
	 */
	public Navigator(Odometer odometer, SimpleInterrupter si) {
		assert(odometer != null);
		assert(si != null);
		this.odometer = odometer;
		this.si = si;

		// - Reset the motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {	Robot.leftMotor,
																			Robot.rightMotor }) {
			motor.stop();
			// - Lower the acceleration of the robot to avoid the slipping on
			// - the surface
			motor.setAcceleration(1000);
		}
	}

	/**
	 * Go forward a set distance in cm
	 * 
	 * @param distance distance to go forward
	 */
	public void goForward(double distance) {
		double x = Math.cos(this.odometer.getOrientation()) * distance;
		double y = Math.sin(this.odometer.getOrientation()) * distance;
		this.travelTo(this.pos.getX() + x, this.pos.getY() + y);
	}

	/**
	 * This method returns true if another thread has called travelTo() or
	 * turnTo() and the method has yet to return; false otherwise.
	 *
	 * @return True if navigation is enable, false otherwise
	 */
	public boolean isNavigating() {
		synchronized (this.lock) {
			return (Math.abs(this.target.getX() -
								this.pos.getX()) > POSITION_ERROR ||
					Math.abs(this.target.getY() -
								this.pos.getY()) > POSITION_ERROR);
		}
	}

	/**
	 * Changes the forward speed used by travelTo method
	 * 
	 * @param ForwardSpeed new magnitude for the forward speed
	 */
	public void setForwardSpeed(int ForwardSpeed) {
		synchronized (this.lock) {
			this.FORWARD_SPEED = Math.abs(ForwardSpeed);
		}
	}

	/**
	 * Changes the rotation speed used by turnTo method
	 * 
	 * @param RotationSpeed new magnitude for the rotation speed
	 */
	public void setRotationSpeed(int RotationSpeed) {
		synchronized (this.lock) {
			this.ROTATE_SPEED = Math.abs(RotationSpeed);
		}
	}

	/**
	 * travelTo takes in the vector of the destination position and travels
	 * to that location
	 * 
	 * @param x x-destination
	 * @param y y-destination
	 */
	public void travelTo(double x, double y) {
		this.travelTo(Vec2D.getVector(x, y));
	}

	/**
	 * travelTo takes in the vector of the destination position and
	 * travels to that location with default stop = true
	 * 
	 * @param point destination
	 */
	public void travelTo(Vec2D point) {
		this.travelTo(point, true);
	}

	/**
	 * travelTo takes in the vector of the destination position and travels
	 * to that location
	 * 
	 * @param point destination
	 * @param stop A flag indicating whether robot should stop or not after
	 *            reaching the destination
	 */
	@SuppressWarnings({ "nls", "boxing" })
	public void travelTo(Vec2D point, boolean stop) {
		assert(point != null);
		if (true) {
			this.target.setVector(point);
			this.pos = this.odometer.getPosition();

			Vec2D dest = this.target;

			while (Math.abs(dest.getX() - this.pos.getX()) > POSITION_ERROR ||
					Math.abs(dest.getY() - this.pos.getY()) > POSITION_ERROR) {
				double minAng = Math.atan2(dest.getY() -this.pos.getY(),
											dest.getX() - this.pos.getX());

				if (minAng < 0)
					minAng += 2 * Math.PI;

				Robot.textLCD.drawString(String.format(	"<%.2f, %.2f>",
														dest.getX(),
														dest.getY()),
											0, 5);
				Robot.textLCD.drawString(String.format(	"<%.2f, %.2f>",
														this.pos.getX(),
														this.pos.getY()),
											0, 6);

				this.turnTo(minAng, true);
				Robot.setSpeeds(this.FORWARD_SPEED, this.FORWARD_SPEED);
				this.pos = this.odometer.getPosition();

				if (this.si.getInterrupt()) {
					System.out.println("Navigator stopped");
					break;
				}
			}
			if (stop)
				Robot.setSpeeds(0, 0);
		}
	}

	/**
	 * Turns the robot by angle rad
	 * 
	 * @param angle angle [rad]
	 * @param stop stop the robot after the rotation of not
	 */
	public void turnTo(double angle, boolean stop) {
		double error = angle - this.odometer.getOrientation();

		while (Math.abs(error) > ANGLE_ERROR) {

			error = angle - this.odometer.getOrientation();

			if (error < -Math.PI) {
				Robot.setSpeeds(-this.ROTATE_SPEED, this.ROTATE_SPEED);
			} else if (error < 0.0) {
				Robot.setSpeeds(this.ROTATE_SPEED, -this.ROTATE_SPEED);
			} else if (error > Math.PI) {
				Robot.setSpeeds(this.ROTATE_SPEED, -this.ROTATE_SPEED);
			} else {
				Robot.setSpeeds(-this.ROTATE_SPEED, this.ROTATE_SPEED);
			}
		}

		if (stop) {
			Robot.setSpeeds(0, 0);
		}
	}
}
