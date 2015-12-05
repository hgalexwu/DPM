package Filters;

/**
 * A basic interface to a filter.
 * 
 * @param <T> Type of the sample to be filtered
 */
public interface Filter<T> {

	/**
	 * Filters a sample using a strategy defined by the implementing class
	 * 
	 * @param x value of the current sample
	 * @return filtered result
	 */
	public T filterSample(T x);

}
