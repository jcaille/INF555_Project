import Jcg.geometry.Point_2;


public class ProjectUtils {

	public ProjectUtils() {
	}

	public static Point_2 getThirdTriangleVertex(double b0, double b1, double d0,
			double d1, int tau) {
		// Get the third vertex (x,y) of a triangle whose base side lies on the
		// x axis,
		// b0 and b1 are the coordinates of the base vertex
		// D0 and D1 the length of each side and tau the direction in which the
		// third vertex lies

		assert (b1 >= b0);
		assert (b1 - b0 <= d1 + d0);
		assert (Math.abs(tau) == 1);

		double x = (d0 * d0 - d1 * d1 + b1 * b1 - b0 * b0) / (2 * (b1 - b0));
		double y = tau * Math.sqrt(d0 * d0 - (x - b0) * (x - b0));

		return new Point_2(x, y);
	}
}
