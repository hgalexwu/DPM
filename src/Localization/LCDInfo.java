package Localization;

import EV3Hardware.Robot;
import MeasurementDevices.Odometer;
import Utility.TimerInterface;
import Utility.Vec2D;
import lejos.hardware.lcd.TextLCD;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * LCD Info
 */
public class LCDInfo implements TimerListener, TimerInterface {
	/**
	 * Suggested LCD tick rate [ms]
	 */
	public static final int LCD_REFRESH = 100;

	/**
	 * LCD of the robot
	 */
	private TextLCD LCD = Robot.textLCD;

	/**
	 * Timer instance
	 */
	private Timer lcdTimer;

	/**
	 * Odometer instance
	 */
	private Odometer odo;

	/**
	 * @param odo Odometer instance
	 */
	public LCDInfo(Odometer odo) {
		assert(odo != null);
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);

		// - Start the timer
		this.lcdTimer.start();
	}

	/**
	 * @see Utility.TimerInterface#start()
	 */
	@Override
	public void start() {
		this.lcdTimer.start();
	}

	/**
	 * @see Utility.TimerInterface#stop()
	 */
	@Override
	public void stop() {
		this.lcdTimer.stop();
	}

	/**
	 * Iteration of the display loop
	 */
	@Override
	@SuppressWarnings("nls")
	public void timedOut() {
		Vec2D pos = this.odo.getPosition();
		this.LCD.clear();
		this.LCD.drawString("X: ", 0, 0);
		this.LCD.drawString("Y: ", 0, 1);
		this.LCD.drawString("H: ", 0, 2);
		this.LCD.drawInt((int) (pos.getX()), 3, 0);
		this.LCD.drawInt((int) (pos.getY()), 3, 1);
		this.LCD.drawInt((int) (Math.toDegrees(this.odo.getOrientation())),
		                 					   3, 2);
	}
}
