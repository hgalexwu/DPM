package Filters;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import Utility.Vec2D;

/**
 * MedianFilterTest
 */
public class MedianFilterTest {

	/**
	 * testComputeMedian
	 */
	@SuppressWarnings({ "static-method", "boxing" })
	@Test
	public void testComputeMedian1() {
		Float[] x = { 20.f, 30.f, 40.f, 50.f, 60.f };
		Float median_expected = 40.f;

		assertTrue(x.length > 1);

		Float median_actual = MedianFilter.computeMedian(x);

		assertTrue(Vec2D.isEqual(median_actual, median_expected, 0.0000001));
	}

	/**
	 * testComputeMedian
	 */
	@SuppressWarnings({ "static-method", "boxing" })
	@Test
	public void testComputeMedian2() {
		Float[] x = { 20.f, 30.f, 40.f, 50.f, 60.f, 70.f };
		Float median_expected = 45.f;

		assertTrue(x.length > 1);

		Float median_actual = MedianFilter.computeMedian(x);

		assertTrue(Vec2D.isEqual(median_actual, median_expected, 0.0000001));
	}

	/**
	 * testFilterSample
	 */
	@SuppressWarnings({ "static-method", "boxing" })
	@Test
	public void testFilterSample1() {
		MedianFilter mf = new MedianFilter(5);

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
		Float actual = 0.f;
		Float expected = 20.f;

		assertTrue(x.length > 1);

		for (Float e : x) {
			actual = mf.filterSample(e);
			assertTrue(Vec2D.isEqual(actual, expected, 0.000001));
		}
	}

	/**
	 * testFilterSample
	 */
	@SuppressWarnings({ "boxing", "static-method" })
	@Test
	public void testFilterSample2() {
		MedianFilter mf = new MedianFilter(5);

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
						20.f };
		Float actual = 0.f;
		Float expected = 20.f;

		assertTrue(x.length > 1);

		for (Float e : x) {
			actual = mf.filterSample(e);
			assertTrue(Vec2D.isEqual(actual, expected, 0.000001));
		}
	}

}
