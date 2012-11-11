import Jcg.geometry.Point_2;
import Jcg.geometry.Point_3;
import Jcg.polyhedron.Halfedge;


public class Window {
	//Represents a windows that we will propagate on the mesh
	
	Halfedge<Point_3> edge ;
	double b0 ; //Distance from nearest window's end to edge.vertex
	double b1 ; //Distance from farthest window's end to edge.vertex
	double d0 ; //Geodesic distance from b0 to the source
	double d1 ; //Geodesic distance from b1 to the source
	int tau   ; //represents the direction in which the source lies. 1 if source is in the direction of edge.face, -1 if not.
	
	public Window() {
		// TODO Auto-generated constructor stub
	}
	
	public Point_2 getThirdTriangleVertex(double b0, double b1, double d0, double d1, int tau){
		//Get the third vertex (x,y) of a triangle whose base side lies on the x axis, 
		//b0 and b1 are the coordinates of the base vertex
		//D0 and D1 the length of each side and tau the direction in which the third vertex lies
		
		assert(b1>=b0);
		assert(b1-b0 <= d1+d0);
		assert(Math.abs(tau) == 1);
		
		double x = (d0*d0 - d1*d1 + b1*b1 - b0 * b0)/(2*(b1-b0));
		double y = tau * Math.sqrt(d0*d0 - (x-b0)*(x-b0)) ;
		
		return new Point_2(x,y);
	}
	
	public Point_2 getSourceInPlane(){
		return null ;
	}

	public Window[] propagate() {
		Window[] res = new Window[2] ;
		//Assuming b0 is not 0 and b1 is not edge.length() (meaning no saddle point as defined in the paper)
		
		
		return res ;
	}
}
