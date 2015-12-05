package Navigator;

import Controllers.PController;
import EV3Hardware.Robot;
import MeasurementDevices.Odometer;
import Sensors.UltrasonicSensor;
import Utility.SimpleInterrupter;
import Utility.TimerInterface;
import lejos.hardware.Sound;
import lejos.utility.Delay;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * Obstacle avoider class allows the robot a 'safe' navigation in the field
 */
public class ObstacleAvoider implements TimerListener, TimerInterface {

	/**
	 * Threshold for applying obstacle avoidance [cm]
	 */
	private static final int THRESHOLD = 14;

	/**
	 * Suggested tick rate for the obstacle avoider [ms]
	 */
	public static final int TICK_RATE = 25;

	/**
	 * Previous angle [rad]
	 */
	private double changeInAngle = 0.0;

	/**
	 * Is the avoidance engaged
	 */
	private boolean isAvoidanceEnabled = false;

	/**
	 * Flag indicating that the obstacle avoider is running
	 */
	private boolean isRunning;

	/**
	 * Timer for the obstacle avoider
	 */
	private Timer oaTimer;

	/**
	 * Odometer for obstacle avoidance
	 */
	private Odometer odo;

	/**
	 * Flag indicating that obstacle avoidance is started
	 */
	private boolean oSstarted = false;

	/**
	 * Angle before avoidance [rad]
	 */
	private double previousAngle = 0.0;

	/**
	 * SimpleInterrupter needed to signal that we need to avoid the object
	 */
	private SimpleInterrupter si;

	/**
	 * Ultrasonic sensor at an angle
	 */
	private UltrasonicSensor usAngledSensor;

	/**
	 * Ultrasonic sensor in front
	 */
	private UltrasonicSensor usFrontSensor;

	/**
	 * P-Controller
	 */
	private PController wfControl;

	/**
	 * Construct the obstacle avoider
	 * 
	 * @param odo Odometer
	 * @param nav Navigator
	 * @param si SimpleInterrupter instance
	 * @param usASensor Instance of the angled ultrasonic sensor
	 * @param usFSensor Instance of the front ultrasonic sensor
	 */
	public ObstacleAvoider(	Odometer odo, SimpleInterrupter si,
							UltrasonicSensor usASensor,
							UltrasonicSensor usFSensor) {
		assert(odo != null);
		assert(si != null);
		assert(usASensor != null);
		assert(usFSensor != null);

		this.odo = odo;
		this.si = si;
		this.usAngledSensor = usASensor;
		this.usFrontSensor = usFSensor;

		this.wfControl = new PController(15, 3, this.usAngledSensor);

		// - Create a timer
		this.oaTimer = new Timer(ObstacleAvoider.TICK_RATE * 2, this);
		this.oaTimer.start();
		this.isRunning = false;

		this.changeInAngle = 0;
	}

	/**
	 * @see Utility.TimerInterface#start()
	 */
	@Override
	public void start() {
		this.isRunning = true;
	}

	/**
	 * @see Utility.TimerInterface#stop()
	 */
	@Override
	public void stop() {
		this.isRunning = false;
	}

	/**
	 * Logic of the obstacle avoider
	 */
	@SuppressWarnings({ "boxing" })
	@Override
	public void timedOut() {
		// - Check if we need to run an iteration
		if (!this.isRunning)
			return;

		// - Fetch a sample from the sensor
		int frontDistance = this.usFrontSensor.getSample()[0];

		// - Trigger the avoidance logic
		if (frontDistance < THRESHOLD && !this.oSstarted) {
			this.oSstarted = true;
			this.si.setInterrupt(true);
			// - Make sure navigator is stopped
			Delay.msDelay(1000);
			Robot.rotateAngle(150, 150);
			Robot.setSpeeds(0, 0);
			// - Turn to the right and start all follower
			this.isAvoidanceEnabled = true;
			this.wfControl.start();
			this.changeInAngle = 0.;
			this.previousAngle = this.odo.getOrientation();
		} else if (frontDistance < THRESHOLD) {
			Robot.rotateAngle(150, 150);
			this.changeInAngle = 0.;
			this.previousAngle = this.odo.getOrientation();
		}

		// - Perform the avoidance task
		if (this.isAvoidanceEnabled) {
			double curAngle = this.odo.getOrientation();
			double tmpChangeInAngle = curAngle - this.previousAngle;

			// - Wrap around logic for corner cases
			if (tmpChangeInAngle > 1.5 * Math.PI) {
				this.previousAngle += Math.PI * 2;
			} else if (tmpChangeInAngle < -1.5 * Math.PI) {
				this.previousAngle -= Math.PI * 2;
			}
			// - Update the current change in angle
			tmpChangeInAngle = curAngle - this.previousAngle;

			// - Update the cumulative change in angle
			this.changeInAngle += tmpChangeInAngle;
			if (this.changeInAngle > Math.toRadians(45)) {
				this.isAvoidanceEnabled = false;
				this.wfControl.stop();
				while (this.wfControl.getPControllerNavigating())
					Delay.msDelay(1000);

				Robot.setSpeeds(0, 0);
				Sound.beepSequenceUp();
				Sound.beepSequenceUp();
				Sound.beepSequenceUp();
				this.si.setInterrupt(false);
				this.oSstarted = false;
			}

			this.previousAngle = curAngle;
		}
	}
}
