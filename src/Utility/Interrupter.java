package Utility;

/**
 * Interrupter is an interface that is meant to be passed as a parameter.
 * The call to setInterrupt will notify the object and getObject will be
 * polled by the object and the object should react accordingly.
 * Classes implementing this interface must use private lock on all reads and
 * writes to the variable storing the interruption status.
 */
public interface Interrupter {

	/**
	 * This method returns whether the current operation should be interrupted
	 * or not.
	 * 
	 * @return True if one should interrupt, false otherwise
	 */
	public boolean getInterrupt();

	/**
	 * Sets the interruption status to communicate to another object.
	 * 
	 * @param ip interruption parameter which when set to true causes
	 *            the object to stop what it is doing
	 */
	public void setInterrupt(boolean ip);
}
