package ObjectLocalization;

import EV3Hardware.Robot;
import FlagCapturer.Capturer;
import Sensors.LightSensorColor;
import Sensors.UltrasonicSensor;
import Utility.TimerInterface;
import Utility.Vec2D;
import lejos.utility.Delay;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * ObjectDetecter class that detects if the object is a styrofoam block
 * or a wooden block
 */
public class ObjectDetecter implements TimerListener, TimerInterface {

	/**
	 * The update period of the object detecter [ms]
	 */
	private static final int TICK_RATE = 10;

	/**
	 * ColorID returned by the light sensor
	 */
	private float color;

	/**
	 * Color of the block we need to capture
	 */
	private int colorCode;

	/**
	 * Timer for automatic polling
	 */
	private Timer detecterTimer;

	/**
	 * distance returned by the US sensor
	 */
	private int distance;

	/**
	 * LightColor Sensor sampler instance
	 */
	private LightSensorColor lsSampler;

	/**
	 * Ultrasonic Sensor sampler instance
	 */
	private UltrasonicSensor usSampler;

	/**
	 * Class performing object detection
	 * 
	 * @param autostart flag indicating whether we should automatically start
	 *            the timer or not
	 * @param colorCode
	 */
	public ObjectDetecter(boolean autostart, int colorCode) {
		// - Setup ultrasonic sensor
		this.usSampler = new UltrasonicSensor(Robot.usSensor, true, false, 5);
		this.colorCode = colorCode;

		// - Setup color sensor
		this.lsSampler = new LightSensorColor(	Robot.blockSensor, true, false,
												5);
		// - Initialize the timer
		this.detecterTimer = new Timer(TICK_RATE, this);
		if (autostart) {
			this.detecterTimer.start();
		}
	}

	/**
	 * Setter method to set color of the block to capture
	 * 
	 * @param color 1->LightBlue;2->RedBlock;3->Yellow;4->White;5->DarkBlue
	 */
	public void setColorCode(int color) {
		this.colorCode = color;
	}

	/**
	 * @see Utility.TimerInterface#start()
	 */
	@Override
	public void start() {
		this.detecterTimer.start();
	}

	/**
	 * @see Utility.TimerInterface#stop()
	 */
	@Override
	public void stop() {
		this.detecterTimer.stop();
	}

	/**
	 * 
	 */
	@SuppressWarnings({ "boxing", "nls" })
	@Override
	public void timedOut() {
		this.color = this.lsSampler.getSample()[0];
		this.distance = this.usSampler.getSample()[0];

		Robot.textLCD.drawString("COLOR : " + this.color, 0, 0);
		Robot.textLCD.drawString("DIST  : " + this.distance, 0, 1);

		// - Detect a void or nothingness...
		if (Vec2D.isEqual(-1.0, this.color, 0.5)) {
			Robot.textLCD.drawString("NO OBJECT   ", 0, 4);
		}

		// - Detect a wooden block
		else if (Vec2D.isEqual(13.0, this.color, 0.5) && this.distance <= 5) {
			Robot.textLCD.drawString("WOODEN BLOCK", 0, 4);
		}
		// - Detect a light blue block
		else if (Vec2D.isEqual(6.0, this.color, 0.5) && this.distance <= 4) {
			Robot.textLCD.drawString("LIGHTBLUE BLOCK  ", 0, 4);
			if (this.colorCode == 1) {
				Capturer.setFound(true, true);
			} else
				Capturer.setFound(true, false);

		} else if (Vec2D.isEqual(2.0, this.color, 0.5) && this.distance <= 4) {
			Robot.textLCD.drawString("DARKBLUE BLOCK  ", 0, 4);
			if (this.colorCode == 5) {
				Capturer.setFound(true, true);
			} else
				Capturer.setFound(true, false);
		} else if (Vec2D.isEqual(0.0, this.color, 0.5) && this.distance <= 4) {
			Robot.textLCD.drawString("RED BLOCK  ", 0, 4);
			if (this.colorCode == 2) {
				Capturer.setFound(true, true);
			} else
				Capturer.setFound(true, false);

		} else if (Vec2D.isEqual(3.0, this.color, 0.5) && this.distance <= 4) {
			Robot.textLCD.drawString("YELLOW BLOCK  ", 0, 4);
			if (this.colorCode == 3) {
				Capturer.setFound(true, true);
			} else
				Capturer.setFound(true, false);
		} else if (this.distance <= 4 && Vec2D.isEqual(7.0, this.color, 0.5)) {
			Robot.textLCD.drawString("WHITE BLOCK  ", 0, 4);
			if (this.colorCode == 4) {
				Capturer.setFound(true, true);
			} else
				Capturer.setFound(true, false);
		}

		// - Slow down the loop
		Delay.msDelay(10);
	}
}
