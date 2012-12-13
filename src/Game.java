import java.util.HashMap;

import Jcg.geometry.Point_3;
import Jcg.polyhedron.Face;
import Jcg.polyhedron.Polyhedron_3;
import Jcg.polyhedron.Vertex;


public class Game {
	
	Point_3 source;
	Face<Point_3> sourceFace;
	Polyhedron_3<Point_3> referencePolyhedron ;
	Polyhedron_3<Point_3> subdividedPolyhedron ;
	MeshSubdivision subdivider ;
	
	GeodesicDistance geodesicDistance ;
	HashMap<Point_3, Double> computedDistanceField ;
	HashMap<Face<Point_3>, Face<Point_3>> correspondingReferenceFace ;
	
	public Game(Polyhedron_3<Point_3> polyhedron3D, Face<Point_3> sourceFace) {
		this.referencePolyhedron = polyhedron3D ;
		this.subdividedPolyhedron = polyhedron3D ;
		this.sourceFace = sourceFace ;
		
		this.correspondingReferenceFace = new HashMap<Face<Point_3>, Face<Point_3>>() ;
		this.computedDistanceField = new HashMap<Point_3, Double>();
		this.geodesicDistance = new GeodesicDistance(referencePolyhedron, sourceFace) ;
		this.subdivider = new LoopSubdivision(subdividedPolyhedron, correspondingReferenceFace);
		
		for(Face<Point_3> f : this.referencePolyhedron.facets){
			this.correspondingReferenceFace.put(f, f);
		}
		
		updateDistanceField();
	}
	
	public void subdivide() {
		this.subdivider.subdivide() ;
	}
	
	public void updateDistanceField() {
		for(Vertex<Point_3> v : this.subdividedPolyhedron.vertices){
			if(!computedDistanceField.containsKey(v.getPoint())){
				Face<Point_3> oldFace = this.correspondingReferenceFace.get(v.getHalfedge().getFace());
				computedDistanceField.put(v.getPoint(), geodesicDistance.distanceToSource(v.getPoint(), oldFace));
			}
		}
	}

}
