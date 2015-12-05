package Utility;

/**
 * SimpleInterrupter is a basic implementation of the Interrupter interface
 */
public final class SimpleInterrupter implements Interrupter {

	/**
	 * Interruption status
	 */
	private boolean interruptionStatus = false;

	/**
	 * Lock for mutual exclusion
	 */
	private final Object lock = new Object();

	/**
	 * Implements getInterrupt from Interrupter interface
	 */
	@Override
	public boolean getInterrupt() {
		synchronized (this.lock) {
			return this.interruptionStatus;
		}
	}

	/**
	 * Implements setInterrupt from Interrupter interface
	 */
	@Override
	public void setInterrupt(boolean ip) {
		synchronized (this.lock) {
			this.interruptionStatus = ip;
		}
	}
}
