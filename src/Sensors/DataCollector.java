package Sensors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import lejos.utility.Delay;

/**
 * Class for collecting data from sensors
 */
public class DataCollector {

	/**
	 * Collect data from the sensor named 'Sensor Name' and writes is to the 
	 * file 'SensorName-Time.csv'
	 * 
	 * @param <T> Type of data collected
	 * @param pDataInstance Instance of a data provider
	 * @param sensorName The name of the sensor
	 * @param samples Number of samples to collect
	 * @param collectionTick Time to fetch a sample
	 * @return If collection was successful or not
	 */
	@SuppressWarnings({ "nls", "boxing", "unused" })
	public static <T> boolean collectData(	DataProvider<T> pDataInstance,
											String sensorName, int samples,
											int collectionTick) {
		assert(pDataInstance != null);
		assert(sensorName != null);
		assert(collectionTick > 0);

		try (PrintWriter writer = new PrintWriter(	String.format("%s-%d.csv",
																sensorName,
																System.currentTimeMillis()),
		                                          				"UTF-8")) {
			for (int i = 0; i < samples; i++) {
				T[] data = pDataInstance.getSample();
				for (T e : data)
					writer.write(e + ", ");
				writer.write("\n");
				writer.flush();

				Delay.msDelay(collectionTick);
			}
		} catch (FileNotFoundException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
		}

		return true;
	}
}
