import java.util.LinkedList;

import Jcg.geometry.Point_2;
import Jcg.geometry.Point_3;
import Jcg.polyhedron.Face;
import Jcg.polyhedron.Halfedge;


public class ProjectUtils {
	
	static double standardPrecision = 0.00000005;

	public ProjectUtils() {
	}
	
	public static boolean equals(double d1, double d2){
		return(Math.abs(d1-d2) <= standardPrecision);
	}
	
	protected static boolean sumMatch(Point_3 A, Point_3 B, Point_3 M){
		//We do check if one of them is null, returning false in this case
		if( A == null || B == null || M == null){
			return false ;
		}
		return Math.abs((Double) A.distanceFrom(B) - (Double) A.distanceFrom(M) - (Double) M.distanceFrom(B)) < standardPrecision ;
	}
	
	public static boolean isSameFace(Face<Point_3> f1, Face<Point_3> f2)
	{
		LinkedList<Point_3> l1 = getVertices(f1), l2 = getVertices(f2);
		
		if(l1.size() != l2.size())
		{
			return(false);
		}
		
		for(Point_3 P1 : l1)
		{
			boolean matchFound = false;
			for(Point_3 P2 : l2)
			{
				if(equals(P1.distanceFrom(P2).doubleValue(),0))
				{
					matchFound = true;
					break;
				}
			}
			
			if(!matchFound)
			{
				return(false);
			}
		}
		return(true);
	}
	
	public static LinkedList<Point_3> getVertices(Face<Point_3> f)
	{
		LinkedList<Point_3> result = new LinkedList<Point_3>();
		Halfedge<Point_3> h = f.getEdge(), next = h.getNext();
		result.push(h.getVertex().getPoint());
		
		while(next != h)
		{
			result.push(next.getVertex().getPoint());
			next = next.getNext();
		}
		
		return(result);
	}

	public static Point_2 getThirdTriangleVertex(double b0, double b1, double d0,
			double d1, int tau) {
		// Get the third vertex (x,y) of a triangle whose base side lies on the
		// x axis,
		// b0 and b1 are the coordinates of the base vertex
		// D0 and D1 the length of each side and tau the direction in which the
		// third vertex lies

		assert (b1 > b0);
		assert (b1 - b0 <= d1 + d0);
		assert (Math.abs(tau) == 1);

		if(b1 == b0){
		}
		double x = (d0 * d0 - d1 * d1 + b1 * b1 - b0 * b0) / (2 * (b1 - b0));
		double y = 0;
		if (d0 < x-b0){
		} else {
			y = tau * Math.sqrt(d0 * d0 - (x - b0) * (x - b0));
		}
		
		return new Point_2(x, y);
	}
	
	protected static Point_2 barycenter(Point_2 A, Point_2 B, double t){
		//returns (1-t)*A + t*B
		double x = (1-t) * A.x + t * B.x ;
		double y = (1-t) * A.y + t * B.y ;
		return new Point_2(x,y) ;
	}

	protected static Point_3 barycenter(Point_3 A, Point_3 B, double t){
		//returns (1-t)*A + t*B
		double x = (1-t) * A.x + t * B.x ;
		double y = (1-t) * A.y + t * B.y ;
		double z = (1-t) * A.z + t * B.z ;
		return new Point_3(x,y,z) ;
	}
	
	protected static double det(double a, double b, double c, double d) {
		return a * d - b * c;
	}

	protected static Point_2 intersectLines(Point_2 A1, Point_2 A2, Point_2 B1, Point_2 B2) {
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

}
