package MeasurementDevices;

import EV3Hardware.Robot;
import Utility.TimerInterface;
import Utility.Vec2D;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * This class allow to concurrently display the odometry data
 */
public class OdometryDisplay implements TimerListener, TimerInterface {
	/**
	 * Suggested tick rate for the display as an interval in [ms].
	 */
	public static final int TICK_RATE = 200;

	/**
	 * Instance of the odometer
	 */
	private Odometer odometer;

	/**
	 * Timer for automatic polling
	 */
	private Timer odoTimer;

	/**
	 * Constructor for the Odometry Display
	 * 
	 * @param odometer odometer to display
	 * @param autostart Start polling automatically or not
	 */
	public OdometryDisplay(Odometer odometer, boolean autostart) {
		this.odometer = odometer;

		this.odoTimer = new Timer(OdometryDisplay.TICK_RATE, this); // create a timer
		if (autostart)
			this.odoTimer.start();

		// - Clear the display once
		Robot.textLCD.clear();
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
	 * Draws the position and orientation of the robot on the screen
	 */
	@SuppressWarnings({ "boxing", "nls" })
	@Override
	public void timedOut() {
		// store position and orientation values and get the odometry information
		Vec2D pos = this.odometer.getPosition();
		double orientation = this.odometer.getOrientation();

		// clear the lines for displaying odometry information
		Robot.textLCD.drawString("X:              ", 0, 0);
		Robot.textLCD.drawString("Y:              ", 0, 1);
		Robot.textLCD.drawString("T:              ", 0, 2);

		// display the odometry information
		Robot.textLCD.drawString(String.format("%.2f", pos.getX()), 3, 0);
		Robot.textLCD.drawString(String.format("%.2f", pos.getY()), 3, 1);
		Robot.textLCD.drawString(String.format("%.2f", 
		                                       Math.toDegrees(orientation), 2), 3, 2);
	}
}
