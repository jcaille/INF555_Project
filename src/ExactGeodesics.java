import java.util.ArrayList;
import java.util.LinkedList;

import com.sun.tools.apt.util.Bark;

import Jcg.geometry.*;
import Jcg.polyhedron.*;

public class ExactGeodesics {
	
	public Polyhedron_3<Point_3> polyhedron3D;
	public LinkedList<Segment_3> segments;
	public LinkedList<Point_3> points ;
	
	public ExactGeodesics(Polyhedron_3<Point_3> polyhedron3D) {
		this.polyhedron3D=polyhedron3D;
		segments = new LinkedList<Segment_3>();
		points = new LinkedList<Point_3>();
	}
	
	public void compute() throws Exception
	{
		Face<Point_3> f = this.polyhedron3D.facets.get(5) ;
		Point_3 a = f.getEdge().getVertex().getPoint() ;
		Point_3 b = f.getEdge().getOpposite().getVertex().getPoint() ;
		Point_3 c = f.getEdge().getNext().getVertex().getPoint() ;
		
		Point_3 X = Window.barycenter(b, a, 0.55) ;
		Point_3 Y = Window.barycenter(b, a, .66) ;
				
		Point_3 source = Window.barycenter(Window.barycenter(a, b, 0.5), c, 0.5) ;
		double b0 = (Double) b.distanceFrom(X) ; System.out.println(b0) ;
		double b1 = (Double) b.distanceFrom(Y) ; System.out.println(b1) ;
		double d0 = (Double) source.distanceFrom(X) ;
		double d1 = (Double) source.distanceFrom(Y) ;
		
		Window myWindow = new Window(f.getEdge().getOpposite(), b0, b1, d0, d1, -1);
		
		this.points.add(source) ;
				
		LinkedList<Window> windowsToDraw = new LinkedList<Window>() ;
		LinkedList<Window> windowToPropagate = new LinkedList<Window>() ;
		
		windowToPropagate.add(myWindow);

		int i = 0 ;
		while(!windowToPropagate.isEmpty() && i < 4185){
			i++;
			Window w = windowToPropagate.poll() ;
			windowsToDraw.add(w) ;
			LinkedList<Window> newWindows  = w.propagate();
			windowToPropagate.addAll(newWindows) ;
		}

		for(Window w : windowsToDraw){
			this.segments.add(w.getSegment()) ;
		}
	}
}
