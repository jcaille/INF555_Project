import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import Jcg.geometry.Point_3;
import Jcg.polyhedron.Face;
import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Polyhedron_3;
import Jcg.polyhedron.Vertex;


public class LoopSubdivision extends MeshSubdivision {

	HashMap<Face<Point_3>, Face<Point_3>> referenceFaces ;
	
	public LoopSubdivision(Polyhedron_3<Point_3> polyhedron3d, HashMap<Face<Point_3>, Face<Point_3>> referenceFaces) {
		super(polyhedron3d);
		this.referenceFaces = referenceFaces ;
	}

	@Override
	public void subdivide() {
		for(Vertex<Point_3> v : this.polyhedron3D.vertices){
			v.tag = 1 ; // Vertex is not original
		}

		ArrayList<Face<Point_3>> originalFacets = (ArrayList<Face<Point_3>>) this.polyhedron3D.facets.clone() ;

		this.splitEdges();
		
		for(Face<Point_3> f : originalFacets){
			this.subdivideFace(f);
		}
	}

	public HashMap<Halfedge<Point_3>, Point_3> computeEdgePoints(){
		HashMap<Halfedge<Point_3>, Point_3> res = new HashMap<Halfedge<Point_3>, Point_3>();
		for(Halfedge<Point_3> h : this.polyhedron3D.halfedges)	{
			if(!res.containsKey(h.getOpposite()) && !res.containsKey(h)){
				Point_3 v0 = h.getVertex().getPoint() ;
				Point_3 v1 = h.getOpposite().getVertex().getPoint() ;
				Point_3 u = new Point_3() ;
				u.barycenter(new Point_3[] {v0,v1});
				res.put(h, u);
			}
		}
		return res ;
	}
	
	public void splitEdges(){
		HashMap<Halfedge<Point_3>, Point_3> edgePoints = this.computeEdgePoints();
		for(Halfedge<Point_3> h : edgePoints.keySet()){
			this.polyhedron3D.splitEdge(h, edgePoints.get(h));
		}
	}
	
	public void subdivideFace(Face<Point_3> f){
		Face<Point_3> referenceFace = this.referenceFaces.get(f) ;
		
		Halfedge<Point_3> e1 = f.getEdge() ;
		while(e1.getVertex().tag == 1){
			e1 = e1.getNext() ;
		}
		Halfedge<Point_3> e3 = e1.getNext().getNext() ;
		Halfedge<Point_3> e5 = e3.getNext().getNext() ;
		
		e3 = this.polyhedron3D.splitFacet(e1, e3);
		e5 = this.polyhedron3D.splitFacet(e3, e5);
		e1 = this.polyhedron3D.splitFacet(e5, e1);

		//e1, e2 and e3 are now the center face edges. 
		//We add everything to our referenceFace HashMap, collision will happen, but it's mostly OK
		
		this.referenceFaces.remove(f);
		this.referenceFaces.put(e1.getFace(), referenceFace);
		this.referenceFaces.put(e3.getFace(), referenceFace);
		this.referenceFaces.put(e5.getFace(), referenceFace);
		this.referenceFaces.put(e1.opposite.getFace(), referenceFace);
		this.referenceFaces.put(e3.opposite.getFace(), referenceFace);
		this.referenceFaces.put(e5.opposite.getFace(), referenceFace);
	}
	
}
