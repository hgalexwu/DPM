package Sensors;

import Utility.TimerInterface;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/***
 * A class to poll data from the color sensor in a concurrent manner
 */
public class LightSensorColor implements DataProvider<Float>, TimerListener,
				TimerInterface, SwitchableFilter {
	/**
	 * Suggested sampling rate for that sensor as an interval in ms.
	 */
	public static final int TICK_RATE = 10;

	/**
	 * Lock object for mutual exclusion
	 */
	private final Object lock = new Object();

	/**
	 * Array of floats holding the sensor data
	 */
	private float[] lsData;

	/**
	 * Average filter for the sensor
	 */
	private MeanFilter lsFilter;

	/**
	 * Sensor mode for reading data
	 */
	private SensorMode lsMode;

	/**
	 * Timer for automatic polling
	 */
	private Timer lsTimer;

	/**
	 * Use filtering
	 */
	private boolean useFilter;

	/**
	 * Constructs a light sensor poller with a user defined sampling rate
	 * 
	 * @param pEV3ColorSensor An instance of a valid pEV3ColorSensor
	 * @param autostart Start polling automatically or not
	 * @param useFilter Makes the output of the sensor filtered
	 * @param window Window size of the applied filter
	 */
	public LightSensorColor(EV3ColorSensor pEV3ColorSensor, boolean autostart,
							boolean useFilter, int window) {
		assert(pEV3ColorSensor != null);
		// - Get the color id sensor reading mode
		this.lsMode = pEV3ColorSensor.getColorIDMode();
		// - Allocate size for samples
		this.lsData = new float[this.lsMode.sampleSize()];
		// - Initialize a mean filter
		this.lsFilter = new MeanFilter(this.lsMode, window);
		// - Create a timer
		this.lsTimer = new Timer(LightSensor.TICK_RATE, this);
		this.useFilter = useFilter;
		if (autostart)
			this.lsTimer.start();
	}

	/**
	 * A getter for the last value of the EV3's light sensor
	 * 
	 * @return Last value of colorID of the sensor
	 */
	@SuppressWarnings("boxing")
	@Override
	public Float[] getSample() {
		Float[] r = new Float[this.lsData.length];

		synchronized (this.lock) {
			for (int i = 0; i < this.lsData.length; i++)
				r[i] = this.lsData[i];
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
		this.lsTimer.setDelay(delay);
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
		this.lsTimer.start();
	}

	/**
	 * @see Utility.TimerInterface#stop()
	 */
	@Override
	public void stop() {
		this.lsTimer.stop();
	}

	/**
	 * This method fetches and filters a sample from the color sensor.
	 */
	@Override
	public void timedOut() {
		synchronized (this.lock) {
			if (this.useFilter)
				this.lsFilter.fetchSample(this.lsData, 0);
			else
				this.lsMode.fetchSample(this.lsData, 0);
		}
	}
}
