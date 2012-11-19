import java.awt.Point;
import java.util.LinkedList;

import Jcg.geometry.Point_2;
import Jcg.geometry.Point_3;
import Jcg.polyhedron.Halfedge;

public class Window {
	// Represents a windows that we will propagate on the mesh

	Halfedge<Point_3> edge;
	double b0; // Distance from nearest window's end to edge.vertex
	double b1; // Distance from farthest window's end to edge.vertex
	double d0; // Geodesic distance from b0 to the source
	double d1; // Geodesic distance from b1 to the source
	int tau; // represents the direction in which the source lies. 1 if source
				// is in the direction of edge.face, -1 if not.

	public Window() {
		// TODO Auto-generated constructor stub
	}

	public Point_2 getThirdTriangleVertex(double b0, double b1, double d0,
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

	public Point_2 getSourceInPlane() {
		return getThirdTriangleVertex(b0, b1, d0, d1, tau);
	}
	
	public Point_2 barycenter(Point_2 A, Point_2 B, double t){
		//returns (1-t)*A + t*B
		double x = (1-t) * A.x + t * B.x ;
		double y = (1-t) * A.y + t * B.y ;
		return new Point_2(x,y) ;
	}

	public static boolean linesIntersect(double x1, double y1, double x2,
			double y2, double x3, double y3, double x4, double y4) {
		/* FROM : http://www.java-gaming.org/index.php?topic=22590.0 */
		// Return false if either of the lines have zero length
		if (x1 == x2 && y1 == y2 || x3 == x4 && y3 == y4) {
			return false;
		}
		// Fastest method, based on Franklin Antonio's
		// "Faster Line Segment Intersection" topic "in Graphics Gems III" book
		// (http://www.graphicsgems.org/)
		double ax = x2 - x1;
		double ay = y2 - y1;
		double bx = x3 - x4;
		double by = y3 - y4;
		double cx = x1 - x3;
		double cy = y1 - y3;

		double alphaNumerator = by * cx - bx * cy;
		double commonDenominator = ay * bx - ax * by;
		if (commonDenominator > 0) {
			if (alphaNumerator < 0 || alphaNumerator > commonDenominator) {
				return false;
			}
		} else if (commonDenominator < 0) {
			if (alphaNumerator > 0 || alphaNumerator < commonDenominator) {
				return false;
			}
		}
		double betaNumerator = ax * cy - ay * cx;
		if (commonDenominator > 0) {
			if (betaNumerator < 0 || betaNumerator > commonDenominator) {
				return false;
			}
		} else if (commonDenominator < 0) {
			if (betaNumerator > 0 || betaNumerator < commonDenominator) {
				return false;
			}
		}
		if (commonDenominator == 0) {
			// This code wasn't in Franklin Antonio's method. It was added by
			// Keith Woodward.
			// The lines are parallel.
			// Check if they're collinear.
			double y3LessY1 = y3 - y1;
			double collinearityTestForP3 = x1 * (y2 - y3) + x2 * (y3LessY1)
					+ x3 * (y1 - y2); // see
										// http://mathworld.wolfram.com/Collinear.html
			// If p3 is collinear with p1 and p2 then p4 will also be collinear,
			// since p1-p2 is parallel with p3-p4
			if (collinearityTestForP3 == 0) {
				// The lines are collinear. Now check if they overlap.
				if (x1 >= x3 && x1 <= x4 || x1 <= x3 && x1 >= x4 || x2 >= x3
						&& x2 <= x4 || x2 <= x3 && x2 >= x4 || x3 >= x1
						&& x3 <= x2 || x3 <= x1 && x3 >= x2) {
					if (y1 >= y3 && y1 <= y4 || y1 <= y3 && y1 >= y4
							|| y2 >= y3 && y2 <= y4 || y2 <= y3 && y2 >= y4
							|| y3 >= y1 && y3 <= y2 || y3 <= y1 && y3 >= y2) {
						return true;
					}
				}
			}
			return false;
		}
		return true;
	}

	public Point_2 intersectLines(Point_2 A1, Point_2 A2, Point_2 B1, Point_2 B2) {
		/* FROM : http://www.java-gaming.org/index.php?topic=22590.0 */
		// Compute the intersection of two lines.
		// Returns a Point_2 corresponding to the coefficient on the first given
		// line.
		double x1 = A1.x;
		double y1 = A1.y;
		double x2 = A2.x;
		double y2 = A2.y;
		double x3 = B1.x;
		double y3 = B1.y;
		double x4 = B2.x;
		double y4 = B2.y;

//		if (!linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
//			// linesIntersect returns false if the segment do not cross
//			return null;
//		}

		double det1And2 = det(x1, y1, x2, y2);
		double det3And4 = det(x3, y3, x4, y4);
		double x1LessX2 = x1 - x2;
		double y1LessY2 = y1 - y2;
		double x3LessX4 = x3 - x4;
		double y3LessY4 = y3 - y4;
		double det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
		if (det1Less2And3Less4 == 0) {
			// the denominator is zero so the lines are parallel and there's
			// either no solution (or multiple solutions if the lines overlap)
			// so return null.
			return null;
		}
		double x = (det(det1And2, x1LessX2, det3And4, x3LessX4) / det1Less2And3Less4);
		double y = (det(det1And2, y1LessY2, det3And4, y3LessY4) / det1Less2And3Less4);
		return new Point_2(x, y);
	}

	protected static double det(double a, double b, double c, double d) {
		return a * d - b * c;
	}

	protected static double edgeLength(Halfedge<Point_3> h) {
		return (Double) h.getVertex().getPoint()
				.distanceFrom(h.getVertex().getPoint());
	}

	public LinkedList<Window> propagate() throws Exception {
		// It returns a list of new windows on the adjacent edges.
		// We use the same notation as in the article
		LinkedList<Window> res = new LinkedList<Window>();
		double edgeLength = edgeLength(this.edge);
		Point_2 P0 = new Point_2(0, 0);
		Point_2 P1 = new Point_2(edgeLength, 0);
		Point_2 P2 = getThirdTriangleVertex(0, edgeLength,
				edgeLength(this.edge.getNext()),
				edgeLength(this.edge.getPrev()), -1);
		Point_2 S = getSourceInPlane();
		Point_2 B0 = new Point_2(b0 /edgeLength, 0) ;
		Point_2 B1 = new Point_2(b1 / edgeLength, 0) ;

		// Assuming b0 is not 0 and b1 is not edge.length() (meaning no saddle
		// point as defined in the paper)
		if (this.b0 == 0 || this.b1 == edgeLength) {
			System.out.println("We've got a saddle point");
			return res;
		}

		//We first compute the intersection with PO-P2 ;
		
		//Then the intersection with P1-P2 ;
		
		//Finally
		return res;
	}
}
