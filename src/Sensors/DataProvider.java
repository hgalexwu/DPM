package Sensors;

/**
 * @author VL
 * @param <T> Type of the data provided by a sensor
 */
public interface DataProvider<T> {

	/**
	 * Gathers n samples from the sensor and returns a copy of these
	 * 
	 * @return sample acquired by the sensor
	 */
		T[] getSample();
}
