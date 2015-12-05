package Utility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * SimpleInterrupterTest
 */
public class SimpleInterrupterTest {

	/**
	 * testGetInterrupt
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetInterrupt() {
		SimpleInterrupter si = new SimpleInterrupter();
		si.setInterrupt(true);
		assertTrue(si.getInterrupt());
		si.setInterrupt(false);
		assertFalse(si.getInterrupt());
	}

	/**
	 * testSetInterrupt
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetInterrupt() {
		SimpleInterrupter si = new SimpleInterrupter();
		si.setInterrupt(true);
		assertTrue(si.getInterrupt());
		si.setInterrupt(false);
		assertFalse(si.getInterrupt());
	}

}
