package Filters;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * This class implements a windowed median filter.
 */
public class MedianFilter implements Filter<Float> {

	/**
	 * Computes the median value
	 * 
	 * @param values samples to use for median computation
	 * @return Median value of 'values' array
	 */
	@SuppressWarnings("boxing")
	public static Float computeMedian(Float[] values) {
		assert(values != null);
		assert(values.length > 0);
		Float median = 0.f;

		// - Sort elements
		// - The exists a selection algorithm that can find media in O(n) instead
		// - of O(nlog(n))
		Arrays.sort(values);

		// - We have an even number of samples
		if ((values.length & 1) == 0) {
			int left_idx = values.length / 2 - 1;
			int right_idx = left_idx + 1;
			median = (values[left_idx] + values[right_idx]) / 2;

			// - We have an odd number of samples
		} else {
			median = values[values.length / 2];
		}

		return median;
	}

	/**
	 * Number of gathered samples
	 */
	private int gatheredSamples = 0;

	/**
	 * Window data holder
	 */
	private Deque<Float> qnum = new LinkedList<>();

	/**
	 * Temporary buffer used for sorting
	 */
	private Float[] qnumTemp = null;

	/**
	 * Width of the moving window
	 */
	private int width;

	/**
	 * Creates a filter for the window of size width
	 * 
	 * @param width size of the window
	 */
	public MedianFilter(int width) {
		this.width = width;
	}

	/**
	 * This method filters the sample and returns the result
	 */
	@Override
	public Float filterSample(Float x) {
		// - Gather samples before computing the median
		if (this.gatheredSamples < this.width - 1) {
			this.qnum.offerFirst(x);
			this.gatheredSamples++;

			// - Now, we have enough samples to compute the median for the first time
		} else if (this.gatheredSamples < this.width) {
			// - Insert the new element
			this.qnum.offerFirst(x);

			// - Copy the samples to the temporary buffer for sorting
			this.qnumTemp = this.qnum.toArray(new Float[0]);
			Float median = computeMedian(this.qnumTemp);

			return median;
			// - Now, we have enough samples to compute the median for subsequent calls
		} else {
			// - Copy the samples to the temporary buffer for sorting
			this.qnumTemp = this.qnum.toArray(new Float[0]);
			Float median = computeMedian(this.qnumTemp);

			// - Insert the new element and remove the last one
			assert(this.qnum.peekLast() != null);
			this.qnum.offerFirst(x);
			this.qnum.pollLast();

			return median;
		}

		return x;
	}
}
