package Sensors;

import Utility.TimerInterface;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MedianFilter;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * A class to poll data from the ultrasonic sensor in a concurrent manner
 */
public class UltrasonicSensor implements DataProvider<Integer>, TimerListener,
				TimerInterface, SwitchableFilter {
	/**
	 * Suggested sampling rate for that sensor as an interval in [ms].
	 */
	public static final int TICK_RATE = 40;

	/**
	 * Lock object for mutual exclusion
	 */
	private final Object lock = new Object();

	/**
	 * Sample provider gives us data from the sensor
	 */
	private SampleProvider us;

	/**
	 * Array of floats holding the sensor data
	 */
	private float[] usData;

	/**
	 * Use filtering
	 */
	private boolean useFilter;

	/**
	 * Median filter for the sensor
	 */
	private MedianFilter usFilter;

	/**
	 * Timer for automatic polling
	 */
	private Timer usTimer;

	/**
	 * Constructs a light sensor poller with a user defined sampling rate passed
	 * to the Timer constructor
	 * 
	 * @param pEV3UltrasonicSensor An instance of a valid EV3UltrasonicSensor
	 * @param autostart Start polling automatically or not
	 * @param useFilter Makes the output of the sensor filtered
	 * @param window Window size of the applied filter
	 */
	@SuppressWarnings({ "nls" })
	public UltrasonicSensor(EV3UltrasonicSensor pEV3UltrasonicSensor,
							boolean autostart, boolean useFilter, int window) {
		assert(pEV3UltrasonicSensor != null);
		// - usDistance provides samples from this instance
		this.us = pEV3UltrasonicSensor.getMode("Distance");
		// - Allocate size for samples
		this.usData = new float[this.us.sampleSize()];
		// - Initialize a mean filter
		this.usFilter = new MedianFilter(this.us, window);
		// - Create a timer
		this.usTimer = new Timer(UltrasonicSensor.TICK_RATE, this);
		this.useFilter = useFilter;
		if (autostart)
			this.usTimer.start();
	}

	/**
	 * A getter for the value of the EV3's ultrasonic sensor
	 * 
	 * @return A value of intensity of the sensor in the range of [0, 255]
	 */
	@SuppressWarnings("boxing")
	@Override
	public Integer[] getSample() {
		Integer[] r = new Integer[this.usData.length];

		synchronized (this.lock) {
			for (int i = 0; i < this.usData.length; i++)
				r[i] = (int) (this.usData[i] * 100.0);
		}

		return r;
	}

	/**
	 * @return True is filtering is used on the sensor and false otherwise
	 */
	@Override
	public boolean isUseFilter() {
		synchronized (this.lock) {
			return this.useFilter;
		}
	}

	@Override
	public void setPollingRate(int delay) {
		this.usTimer.setDelay(delay);
	}

	/**
	 * Sets the state of filtering allowing to enable or disable it
	 * 
	 * @param useFilter New state of the filtering
	 */
	@Override
	public void setUseFilter(boolean useFilter) {
		synchronized (this.lock) {
			this.useFilter = useFilter;
		}
	}

	/**
	 * @see Utility.TimerInterface#start()
	 */
	@Override
	public void start() {
		this.usTimer.start();
	}

	/**
	 * @see Utility.TimerInterface#stop()
	 */
	@Override
	public void stop() {
		this.usTimer.stop();
	}

	/**
	 * This method fetches a sample from the ultrasonic sensor
	 */
	@Override
	public void timedOut() {
		synchronized (this.lock) {
			if (this.useFilter)
				this.usFilter.fetchSample(this.usData, 0);
			else
				this.us.fetchSample(this.usData, 0);
		}
	}
}
