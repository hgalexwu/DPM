package Filters;

import java.util.Deque;
import java.util.LinkedList;

/**
 * This class implements a windowed average filter
 */
public class AverageFilter implements Filter<Float> {

	/**
	 * Computes the moving average
	 * 
	 * @param pavg previous average
	 * @param x_k current sample
	 * @param x_kn last sample of the previous window
	 * @param window size of the window
	 * @return new average value
	 */
	@SuppressWarnings("boxing")
	public static Float computeMovingAverage(	Float pavg, Float x_k, Float x_kn,
												int window) {
		assert(window != 0);
		return (pavg + (x_k - x_kn) / window);
	}

	/**
	 * Sum of the first n samples and later the average
	 */
	@SuppressWarnings("boxing")
	private Float average = 0.f;

	/**
	 * Number of gathered samples
	 */
	private int gatheredSamples = 0;

	/**
	 * Window data holder
	 */
	private Deque<Float> qnum = new LinkedList<>();

	/**
	 * Width of the moving window
	 */
	private int width;

	/**
	 * Creates a filter for the window of size width
	 * 
	 * @param width size of the window
	 */
	public AverageFilter(int width) {
		this.width = width;
	}

	/**
	 * This method filters the sample and returns the result
	 */
	@SuppressWarnings("boxing")
	@Override
	public Float filterSample(Float x) {
		// - Gather samples before computing the average
		if (this.gatheredSamples < this.width) {
			this.qnum.offerFirst(x);
			this.gatheredSamples++;

			// - Now, we have enough samples to compute the average
			// - and the moving average
		} else if (this.gatheredSamples == this.width) {
			// - Compute the average
			for (Float e : this.qnum)
				this.average += e;

			this.average /= this.width;

			// - Add the new values to the deque and compute the moving average
			this.qnum.offerFirst(x);
			assert(this.qnum.peekLast() != null);
			this.average = computeMovingAverage(this.average, x,
												this.qnum.pollLast(),
												this.width);
			this.gatheredSamples++;

			return this.average;
		} else {
			// - Compute the moving average
			assert(this.qnum.peekLast() != null);
			this.qnum.offerFirst(x);
			this.average = computeMovingAverage(this.average, x,
												this.qnum.pollLast(),
												this.width);

			return this.average;
		}

		return x;
	}
}
