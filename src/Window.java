import java.awt.Point;
import java.util.LinkedList;

import org.hamcrest.core.IsNull;

import Jcg.geometry.Point_2;
import Jcg.geometry.Point_3;
import Jcg.geometry.Segment_3;
import Jcg.polyhedron.Halfedge;

public class Window {
	// Represents a windows that we will propagate on the mesh

	Halfedge<Point_3> edge;
	double sigma ; //Distance from pseudo-source to source
	double b0; // Distance from nearest window's end to edge.vertex
	double b1; // Distance from farthest window's end to edge.vertex
	double d0; // Geodesic distance from b0 to the source
	double d1; // Geodesic distance from b1 to the source
	int tau; // represents the direction in which the source lies. 1 if source
	
	// is in the direction of edge.face, -1 if not.

	public Window(Halfedge<Point_3> edge, double b0, double b1, double d0, double d1, int tau) {
		this.edge = edge ;
		this.b0 = b0 ;
		this.b1 = b1 ;
		this.d0 = d0 ;
		this.d1 = d1 ;
		this.tau = tau ;
		this.sigma = 0;
	}
	
	public Window(){
		
	}

	public Window(Halfedge<Point_3> h, Point_2 P0 , Point_2 B0, Point_2 B1, Point_2 S, int tau, double sigma){
		//construct a window on halfedge h where
		//P0 is the extremity of h, P0 = h.getVertex().getPoint()
		//B0 and B1 are points on h, respectively the start and end of the window
		//S is the source

		this.edge = h ;
		this.b0 = (Double) P0.distanceFrom(B0) ;
		this.b1 = (Double) P0.distanceFrom(B1) ;
		this.d0 = (Double) S.distanceFrom(B0) ;
		this.d1 = (Double) S.distanceFrom(B1) ;
		this.tau = tau ;
		this.sigma = sigma ;
	}
	
	public double getMinDistanceToSource(){
		return this.sigma + Math.min(this.d0, this.d1); 
	}
	
	public double getMaxDistanceToSource(){
		return this.sigma + Math.max(this.d0, this.d1); 
	}

	public boolean isCompatibleWith(Window w){
		assert(this.b0 <= this.b1);
		assert(w.b0 <= w.b1);
		
		if(this.b1 < w.b0 || this.b0 > w.b1){
			return true;
		}
		
		return false ;
	}
	
	public Point_2 getThirdTriangleVertex(double b0, double b1, double d0,
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
			System.out.println("Huho");
		} else {
			y = tau * Math.sqrt(d0 * d0 - (x - b0) * (x - b0));
		}
		
		return new Point_2(x, y);
	}

	public Point_2 getSourceInPlane() {
		return getThirdTriangleVertex(b0, b1, d0, d1, tau);
	}

	public static Point_2 barycenter(Point_2 A, Point_2 B, double t){
		//returns (1-t)*A + t*B
		double x = (1-t) * A.x + t * B.x ;
		double y = (1-t) * A.y + t * B.y ;
		return new Point_2(x,y) ;
	}

	public static Point_3 barycenter(Point_3 A, Point_3 B, double t){
		//returns (1-t)*A + t*B
		double x = (1-t) * A.x + t * B.x ;
		double y = (1-t) * A.y + t * B.y ;
		double z = (1-t) * A.z + t * B.z ;
		return new Point_3(x,y,z) ;
	}

	public static boolean linesIntersect(Point_2 A1, Point_2 A2, Point_2 B1, Point_2 B2) {
		/* FROM : http://www.java-gaming.org/index.php?topic=22590.0 */
		// Return false if either of the lines have zero length or one of the point is null

		if(A1 == null || A2 == null || B1 == null || B2 == null){
			return false ;
		}

		double x1 = A1.x;
		double y1 = A1.y;
		double x2 = A2.x;
		double y2 = A2.y;
		double x3 = B1.x;
		double y3 = B1.y;
		double x4 = B2.x;
		double y4 = B2.y;

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
				.distanceFrom(h.getOpposite().getVertex().getPoint());
	}

	protected static boolean sumMatch(Point_2 A, Point_2 B, Point_2 M, double precision){
		//We do check if one of them is null, returning false in this case
		if( A == null || B == null || M == null){
			return false ;
		}
		return Math.abs((Double) A.distanceFrom(B) - (Double) A.distanceFrom(M) - (Double) M.distanceFrom(B)) < precision ;
	}

	public Segment_3 getSegment(){
		Point_3 firstPoint = barycenter(this.edge.vertex.getPoint(), this.edge.opposite.vertex.getPoint(), b0/edgeLength(this.edge)) ;
		Point_3 secondPoint = barycenter(this.edge.vertex.getPoint(), this.edge.opposite.vertex.getPoint(), b1/edgeLength(this.edge)) ;

		return new Segment_3(firstPoint, secondPoint) ;
	}

	public LinkedList<Window> propagate() throws Exception {
		LinkedList<Window> res = new LinkedList<Window>();
		double edgeLength = edgeLength(this.edge);

		//Compute the points (notations are the same as in the article)
		Point_2 P0 = new Point_2(0, 0);
		Point_2 P1 = new Point_2(edgeLength, 0);
		Point_2 P2 = getThirdTriangleVertex(0, edgeLength,
				edgeLength(this.edge.getNext()),
				edgeLength(this.edge.getPrev()), -this.tau);
		Point_2 S = getSourceInPlane();
		Point_2 B0 = new Point_2(b0, 0) ;
		Point_2 B1 = new Point_2(b1, 0) ;
		
		double standardPrecision = 0.00000005 ;

		boolean case0 = (Double) S.distanceFrom(P0) < standardPrecision ;
		boolean case0bis = (Double) S.distanceFrom(P1) < standardPrecision ;
		
		//CASE 0 AND 0' are treated early
		if ( case0 ){
			//CASE 0
			//We add the two full windows 
			
			Halfedge<Point_3> h = this.edge.next.opposite ;
			Window myWindow = new Window(h, P0, P0, P2, S, this.tau, this.sigma);
			res.push(myWindow) ;
			
			h = this.edge.prev.opposite ;
			myWindow = new Window(h, P2, P2, P1, S, this.tau, this.sigma);
			res.push(myWindow) ;
			
			return res ;
		} else if ( case0bis ){
			//CASE 0
			//We add the two full windows 
			
			Halfedge<Point_3> h = this.edge.next.opposite ;
			Window myWindow = new Window(h, P0, P0, P2, S, this.tau, this.sigma);
			res.push(myWindow) ;
			
			h = this.edge.prev.opposite ;
			myWindow = new Window(h, P2, P2, P1, S, this.tau, this.sigma);
			res.push(myWindow) ;
			
			return res ;
		}
		
		//We compute the intersection with PO-P2 ;
		Point_2 M0 = intersectLines(S,B0, P0,P2);
		Point_2 M1 = intersectLines(S,B1, P0,P2);

		//Then the intersection with P1-P2 ;
		Point_2 M2 = intersectLines(S, B0, P1, P2);
		Point_2 M3 = intersectLines(S, B1, P1, P2);
	
		//We check if those points are valid (meaning if they lie in their respective segments : P1-P2 or P0-P2)
		//standardPrecision is used for rounding errors
		
		boolean isM0Valid = sumMatch(P0, P2, M0, standardPrecision) ;
		boolean isM1Valid = sumMatch(P0, P2, M1, standardPrecision) ;
		boolean isM2Valid = sumMatch(P1, P2, M2, standardPrecision) ;
		boolean isM3Valid = sumMatch(P1, P2, M3, standardPrecision) ;
		
		//We define the cases as precised in the report
		boolean case1 = isM0Valid && isM1Valid && !isM2Valid && isM3Valid && (Double) M3.distanceFrom(P1) < standardPrecision ;
		boolean case2 = isM0Valid && !isM1Valid && isM2Valid && isM3Valid && (Double) M0.distanceFrom(P0) < standardPrecision ;
		boolean case3 = isM0Valid && !isM1Valid && !isM2Valid && isM3Valid ;
		boolean case4 = !isM0Valid && !isM1Valid && isM2Valid && isM3Valid ;
		boolean case5 = isM0Valid && isM1Valid && !isM2Valid && !isM3Valid ;
		
		if(case1){
			//CASE 1
			//We've got a saddle point
			//We add three window, two of them having pseudo-sources
			
			Halfedge<Point_3> h = this.edge.next.opposite ;
			Window myWindow = new Window(h, P0, M0, M1, S, tau, sigma);
			res.push(myWindow);
			
			double newSigma = (Double) P1.distanceFrom(S);

			if( (Double) M1.distanceFrom(P2) > standardPrecision ){
				myWindow = new Window(h, P0, M1, P2, P1, tau, newSigma);
				res.push(myWindow);				
			} else {
				System.out.println("We're in a pretty specific subcase of 1");
			}
			
			h = this.edge.prev.opposite ;
			myWindow = new Window(h, P2, P2, P1, P1, tau, newSigma);
			res.push(myWindow);
			
			return res;
		}
		
		if(case2){
			//CASE 2
			//We've got a saddle point
			//We add three window, two of them having pseudo-sources
			
			Halfedge<Point_3> h = this.edge.prev.opposite ;
			Window myWindow = new Window(h, P2, M2, M3, S, tau, sigma);
			res.push(myWindow);
			
			double newSigma = (Double) S.distanceFrom(P0) ;
			if ((Double) M2.distanceFrom(P2) > standardPrecision) {
				myWindow = new Window(h, P2, P2, M2, P0, tau, newSigma);
				res.push(myWindow);
			} else {
				System.out.println("We're in a pretty specific subcase of 2");
			}

			
			h = this.edge.next.opposite ;
			myWindow = new Window(h, P0, P0, P2, P0, tau, newSigma);
			res.push(myWindow);
			
			return res ;
		}
		if(case3){
			//CASE 3
			//We add two windows with the same source
			
			Halfedge<Point_3> h = this.edge.next.opposite;
			Window myWindow = new Window(h, P0, M0, P2, S, tau, this.sigma);
			res.push(myWindow);
			
			h = this.edge.prev.opposite ;
			myWindow = new Window(h, P2, P2, M3, S, tau, this.sigma);
			res.push(myWindow);
			
			return res ;
		}
		if(case4){
			//CASE 4
			//We add only one window to the P1-P2 edge
			
			Halfedge<Point_3> h = this.edge.prev.opposite ;
			Window myWindow = new Window(h, P2, M2, M3, S, tau, this.sigma);
			res.push(myWindow);
			return res;
		}
		if(case5){
			//CASE 5
			//We add only one window on the P0-P2 edge
			
			Halfedge<Point_3> h = this.edge.next.opposite ;
			Window myWindow = new Window(h, P0, M0, M1, S, tau, sigma);
			res.push(myWindow);
			return res ;
			
		}
		
		System.out.println("Unhandled case") ;
		throw(new Exception("This case is unhandeld right now"));
	}
}