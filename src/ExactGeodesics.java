import java.util.ArrayList;
import java.util.LinkedList;

import Jcg.geometry.*;
import Jcg.polyhedron.*;


public class ExactGeodesics {
	
	public Polyhedron_3<Point_3> polyhedron3D;
	public LinkedList<Segment_3> segments;
	
	public ExactGeodesics(Polyhedron_3<Point_3> polyhedron3D) {
		this.polyhedron3D=polyhedron3D;
		segments = new LinkedList<Segment_3>();
	}
	
	public void compute()
	{
		//segments.add(new Segment_3(V1.getPoint(), V2.getPoint()));
		ArrayList<Halfedge<Point_3>> halfedges = this.polyhedron3D.halfedges;
		Halfedge<Point_3> h = this.polyhedron3D.halfedges.get(100) ;
		segments.add(new Segment_3(h.getVertex().getPoint() , h.getOpposite().getVertex().getPoint()));
	}
}
