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

	public Window(Halfedge<Point_3> edge, double b0, double b1, double d0, double d1, int tau, double sigma) {
		this.edge = edge ;
		this.b0 = b0 ;
		this.b1 = b1 ;
		this.d0 = d0 ;
		this.d1 = d1 ;
		this.tau = tau ;
		this.sigma = sigma;
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
	
	public Window(Window w, double b0, double b1) {
		this.edge = w.edge ;
		this.b0 = b0 ;
		this.b1 = b1 ;
		this.tau = w.tau ;
		this.sigma = w.sigma;
		
		if(b0 == w.b0){
			this.d0 = w.d0 ;
		}else{
			this.d0 = w.distanceOnEdge(b0) ;
		}
		
		if(b1 == w.b1){
			this.d1 = w.d1 ;
		}else{
			this.d1 = w.distanceOnEdge(b1) ;
		}
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
	
	public boolean isEmpty(){
		return ProjectUtils.equals(this.b0, this.b1) ;
	}
	
	public double distanceOnEdge(double x){
		Point_2 X = new Point_2(x, 0);
		return (Double) X.distanceFrom(getSourceInPlane()) + sigma ;
	}
	
	public String toString(){
		String s = "Window of parameters :\n";
		s += "Edge : " + this.edge.toString() ;
		s += "\n Sigma : " + this.sigma + "\n" ;
		return s ;
	}
	

	public Point_2 getSourceInPlane() {
		return ProjectUtils.getThirdTriangleVertex(b0, b1, d0, d1, tau);
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
		Point_3 firstPoint = ProjectUtils.barycenter(this.edge.vertex.getPoint(), this.edge.opposite.vertex.getPoint(), b0/edgeLength(this.edge)) ;
		Point_3 secondPoint = ProjectUtils.barycenter(this.edge.vertex.getPoint(), this.edge.opposite.vertex.getPoint(), b1/edgeLength(this.edge)) ;

		return new Segment_3(firstPoint, secondPoint) ;
	}

	public LinkedList<Window> propagate() throws Exception {
		LinkedList<Window> res = new LinkedList<Window>();
		double edgeLength = edgeLength(this.edge);

		//Compute the points (notations are the same as in the article)
		Point_2 P0 = new Point_2(0, 0);
		Point_2 P1 = new Point_2(edgeLength, 0);
		Point_2 P2 = ProjectUtils.getThirdTriangleVertex(0, edgeLength,
				edgeLength(this.edge.getNext()),
				edgeLength(this.edge.getPrev()), -this.tau);
		Point_2 S = getSourceInPlane();
		Point_2 B0 = new Point_2(b0, 0) ;
		Point_2 B1 = new Point_2(b1, 0) ;
		
		double standardPrecision = 0.00000005 ;

		boolean case0 = (Double) S.distanceFrom(P0) < standardPrecision ;
		boolean case0bis = (Double) S.distanceFrom(P1) < standardPrecision ;
		boolean case0ter = sumMatch(P0, P1, S, standardPrecision) ;
		
		//CASE 0 AND 0' are treated early
		if ( case0 || case0bis || case0ter){
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
		Point_2 M0 = ProjectUtils.intersectLines(S,B0, P0,P2);
		Point_2 M1 = ProjectUtils.intersectLines(S,B1, P0,P2);

		//Then the intersection with P1-P2 ;
		Point_2 M2 = ProjectUtils.intersectLines(S, B0, P1, P2);
		Point_2 M3 = ProjectUtils.intersectLines(S, B1, P1, P2);
	
		//We check if those points are valid (meaning if they lie in their respective segments : P1-P2 or P0-P2)
		//standardPrecision is used for rounding errors
		
		boolean isM0Valid = sumMatch(P0, P2, M0, standardPrecision) ;
		boolean isM1Valid = sumMatch(P0, P2, M1, standardPrecision) ;
		boolean isM2Valid = sumMatch(P1, P2, M2, standardPrecision) ;
		boolean isM3Valid = sumMatch(P1, P2, M3, standardPrecision) ;
		
		//We define the cases as precised in the report
		boolean case1 = isM0Valid && isM1Valid && !isM2Valid && isM3Valid ;// && (Double) M3.distanceFrom(P1) < standardPrecision ;
		boolean case2 = isM0Valid && !isM1Valid && isM2Valid && isM3Valid ; //&& (Double) M0.distanceFrom(P0) < standardPrecision ;
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
			
			double newSigma = (Double) P1.distanceFrom(S) + sigma;

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
			
			double newSigma = (Double) S.distanceFrom(P0) + sigma ;
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