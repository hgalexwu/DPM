package Filters;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import Utility.Vec2D;

/**
 * AverageFilterTest
 */
public class AverageFilterTest {

	/**
	 * testComputeMovingAverage
	 */
	@SuppressWarnings({ "boxing", "static-method" })
	@Test
	public void testComputeMovingAverage() {
		Float[] x = { 20.f, 20.f, 20.f, 20.f, 20.f };
		Float average = 20.f;

		assertTrue(x.length > 1);

		Float moving_avg = AverageFilter.computeMovingAverage(	20.f,
																x[x.length - 1],
																x[0], 4);

		assertTrue(Vec2D.isEqual(average, moving_avg, 0.0000001));
	}

	/**
	 * testFilterSample
	 */
	@SuppressWarnings({ "static-method", "boxing" })
	@Test
	public void testFilterSample() {
		AverageFilter mf = new AverageFilter(5);

		Float[] x = {	20.f,
						20.f,
						20.f,
						20.f,
						20.f,
						20.f,
						20.f,
						20.f,
						20.f,
						20.f,
						20.f,
						20.f,
						20.f,
						20.f,
						20.f };
		Float average = 0.f;
		Float expected = 20.f;

		assertTrue(x.length > 1);

		for (Float e : x) {
			average = mf.filterSample(e);
			assertTrue(Vec2D.isEqual(average, expected, 0.000001));
		}
	}
}
