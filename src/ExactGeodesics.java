import java.util.ArrayList;
import java.util.LinkedList;

import com.sun.tools.apt.util.Bark;

import Jcg.geometry.*;
import Jcg.polyhedron.*;

public class ExactGeodesics {
	
	public Polyhedron_3<Point_3> polyhedron3D;
	public LinkedList<Segment_3> segments;
	
	public ExactGeodesics(Polyhedron_3<Point_3> polyhedron3D) {
		this.polyhedron3D=polyhedron3D;
		segments = new LinkedList<Segment_3>();
	}
	
	public void compute() throws Exception
	{
		Face<Point_3> f = this.polyhedron3D.facets.get(700) ;
		Point_3 a = f.getEdge().getVertex().getPoint() ;
		Point_3 b = f.getEdge().getOpposite().getVertex().getPoint() ;
		Point_3 c = f.getEdge().getNext().getVertex().getPoint() ;
		
		Point_3 X = Window.barycenter(b, a, 0.3) ;
		Point_3 Y = Window.barycenter(b, a, .8) ;
				
		Point_3 source = Window.barycenter(Window.barycenter(a, b, 0.5), c, 0.5) ;
		double b0 = (Double) b.distanceFrom(X) ; System.out.println(b0) ;
		double b1 = (Double) b.distanceFrom(Y) ; System.out.println(b1) ;
		double d0 = (Double) source.distanceFrom(X) ;
		double d1 = (Double) source.distanceFrom(Y) ;
		
		Window myWindow = new Window(f.getEdge().getOpposite(), b0, b1, d0, d1, -1);
		
		this.segments.add(myWindow.getSegment());
		
		LinkedList<Window> newWindows = myWindow.propagate() ;
		for(Window w : newWindows){
			System.out.println(".") ;
			this.segments.add(w.getSegment()) ;
		}
	}
}
