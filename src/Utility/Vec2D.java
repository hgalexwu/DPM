package Utility;

/**
 * Class for handling two dimensional vectors implemented as a mutable object
 */
public final class Vec2D {

	/**
	 * Adds v to u and returns the resulting vector
	 * 
	 * @param v lhs
	 * @param u rhs
	 * @return (v + u)
	 */
	public static Vec2D add(Vec2D v, Vec2D u) {
		assert(v != null && u != null);
		return new Vec2D(v.x + u.x, v.y + u.y);
	}

	/**
	 * Fixes the range of the angle ensuring that it belongs to [0, 2PI]
	 * 
	 * @param angle angle to fix in [rad]
	 * @return fixed angle in range of [0, 2*PI]
	 */
	public static double fixAngle(double angle) {
		if (angle < 0.0)
			return ((2 * Math.PI) + (angle % (2 * Math.PI))) % (2 * Math.PI);

		return angle % (2 * Math.PI);
	}

	/**
	 * Factory function returning a null vector
	 * 
	 * @return return vector with {0.0, 0.0}
	 */
	public static Vec2D getNull() {
		return new Vec2D(0.0, 0.0);
	}

	/**
	 * Factory function returning a vector specified by x, y arguments
	 * 
	 * @param x x-value
	 * @param y y-value
	 * @return return vector with {x, y}
	 */
	public static Vec2D getVector(double x, double y) {
		return new Vec2D(x, y);
	}

	/**
	 * Checks whether {x, y} is close to {tx, ty}
	 * 
	 * @param x current x-value
	 * @param y current x-value
	 * @param tx target x-value
	 * @param ty target y-value
	 * @param dx error for x
	 * @param dy error for y
	 * @return True if {x, y} is close to {tx, ty} and false otherwise
	 */
	public static boolean isCloseTo(double x, double y, double tx, double ty,
									double dx, double dy) {
		if (isInRange(x, tx, tx, dx) && isInRange(y, ty, ty, dy))
			return true;

		return false;
	}

	/**
	 * Checks if u is in ]v - error, v + error[
	 * 
	 * @param u First value used in the comparison
	 * @param v Second value used in the comparison
	 * @param error Amount of tolerance
	 * @return True if u is within the error of v
	 */
	public static boolean isEqual(double u, double v, double error) {
		if (v - error < u && u < v + error)
			return true;
		return false;
	}

	/**
	 * Checks if val is in ]min - error, max + error[
	 * 
	 * @param val Value to check, depends on comparison error
	 * @param min Minimum bound
	 * @param max Maximum bound
	 * @param error Amount of tolerance
	 * @return returns true if val is in ]min - error, max + error[
	 */
	public static boolean isInRange(double val, double min, double max,
									double error) {
		if (min - error < val && val < max + error)
			return true;
		return false;
	}

	/**
	 * Computes the minimum angle between a and b
	 * 
	 * @param a angle a in [rad]
	 * @param b angle b in [rad]
	 * @return minimum angle from a to b
	 */
	public static double minimumAngleFromTo(double a, double b) {
		double d = fixAngle(b - a);

		if (d < Math.PI)
			return d;
		return d - 2 * Math.PI;
	}

	/**
	 * Reduces the range of the angle to fit into -2pi <= angle <= 2pi
	 * 
	 * @param angle angle to range [rad]
	 * @return ranged angle
	 */
	public static double rangeAngle(double angle) {
		double divs = Math.floor(angle / (2.0 * Math.PI));
		return (angle - divs * 2.0 * Math.PI);
	}

	/**
	 * Subtracts u from v vector and returns the resulting vector
	 * 
	 * @param v lhs
	 * @param u rhs
	 * @return (v - u)
	 */
	public static Vec2D sub(Vec2D v, Vec2D u) {
		assert(v != null && u != null);
		return new Vec2D(v.x - u.x, v.y - u.y);
	}

	/**
	 * Component x of the vector.
	 */
	private double x;

	/**
	 * Component y of the vector.
	 */
	private double y;

	/***
	 * Constructor that initializes vector's x and y
	 * 
	 * @param x x-component
	 * @param y y-component
	 */
	private Vec2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Adds v to this vector and returns the result. (this + v)
	 * 
	 * @param v vector to add
	 * @return this
	 */
	public Vec2D add(Vec2D v) {
		assert(v != null);
		this.x += v.x;
		this.y += v.y;
		return this;
	}

	/**
	 * Unsupported functionality needs to be marked as such to avoid logic errors
	 */
	@SuppressWarnings("nls")
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new RuntimeException("Unimplemented function");
	}

	/**
	 * Performs (x - y) operation and returns the result
	 * 
	 * @return Difference of the components
	 */
	public double componentDiff() {
		return this.x - this.y;
	}

	/**
	 * Performs (x + y) operation and returns the result
	 * 
	 * @return Sum of the components
	 */
	public double componentSum() {
		return this.x + this.y;
	}

	/**
	 * Method that returns a copy of this vector
	 * 
	 * @return return a new vector with <this.x, this.y>
	 */
	public Vec2D copyOf() {
		return new Vec2D(this.x, this.y);
	}

	/**
	 * Unsupported functionality needs to be marked as such to avoid logic errors
	 */
	@SuppressWarnings("nls")
	@Override
	public boolean equals(Object arg0) {
		throw new RuntimeException("Unimplemented function");
	}

	/**
	 * Unsupported functionality needs to be marked as such to avoid logic errors
	 */
	@SuppressWarnings("nls")
	@Override
	protected void finalize() throws Throwable {
		throw new RuntimeException("Unimplemented function");
	}

	/**
	 * Returns the x-component of the vector
	 * 
	 * @return x-component
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * Returns the y-component of the vector
	 * 
	 * @return y-component
	 */
	public double getY() {
		return this.y;
	}

	/**
	 * Unsupported functionality needs to be marked as such to avoid logic errors
	 */
	@SuppressWarnings("nls")
	@Override
	public int hashCode() {
		throw new RuntimeException("Unimplemented function");
	}

	/**
	 * Computes the norm of the vector
	 * 
	 * @return the value of the norm
	 */
	public double norm() {
		return Math.sqrt(Math.pow(this.x, 2.0) + Math.pow(this.y, 2.0));
	}

	/**
	 * Scales this vector by k
	 * 
	 * @param k scaling factor
	 * @return this
	 */
	public Vec2D scale(double k) {
		this.x *= k;
		this.y *= k;
		return this;
	}

	/**
	 * Scales this vector by k_x on x and k_y on y
	 * 
	 * @param k_x scaling factor for x
	 * @param k_y scaling factor for y
	 * @return this
	 */
	public Vec2D scale(double k_x, double k_y) {
		this.x *= k_x;
		this.y *= k_y;
		return this;
	}

	/**
	 * Set this vector to have <x, y> as components
	 * 
	 * @param x x-component to set
	 * @param y y-component to set
	 */
	public void setComponent(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Set this vector to represent the magnitude at an angle [rad]
	 * 
	 * @param magnitude magnitude
	 * @param angle angle in [rad]
	 */
	public void setMagAngle(double magnitude, double angle) {
		this.x = magnitude * Math.cos(angle);
		this.y = magnitude * Math.sin(angle);
	}

	/**
	 * Set this vector to be identical to v. (this = v)
	 * 
	 * @param v vector whose component will be copied
	 */
	public void setVector(Vec2D v) {
		assert(v != null);
		this.x = v.x;
		this.y = v.y;
	}

	/**
	 * Sets the x-component of the vector
	 * 
	 * @param x x-component
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Sets the y-component of the vector
	 * 
	 * @param y y-component
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Subtracts v from this vector. (this - v)
	 * 
	 * @param v vector to subtract
	 * @return this
	 */
	public Vec2D sub(Vec2D v) {
		assert(v != null);
		this.x -= v.x;
		this.y -= v.y;
		return this;
	}

	/**
	 * Returns a string representation of the vector
	 */
	@SuppressWarnings({ "boxing", "nls" })
	@Override
	public String toString() {
		return String.format("<%f, %f>", this.x, this.y);
	}
}
