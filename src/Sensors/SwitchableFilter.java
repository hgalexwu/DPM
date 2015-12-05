package Sensors;

/**
 * An interface allowing the filter to be switched on or off
 */
public interface SwitchableFilter {

	/**
	 * @return True is filtering is used on the sensor and false otherwise
	 */
	public boolean isUseFilter();

	/**
	 * Sets the polling rate of the sensor
	 * 
	 * @param delay
	 */
	public void setPollingRate(int delay);

	/**
	 * Sets the state of filtering allowing to enable or disable it
	 * 
	 * @param useFilter New state of the filtering
	 */
	public void setUseFilter(boolean useFilter);

}
