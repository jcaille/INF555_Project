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
	
	public Game(Polyhedron_3<Point_3> polyhedron3D, Polyhedron_3<Point_3> polyhedron3DBis, Face<Point_3> sourceFace) {
		this.referencePolyhedron = polyhedron3D ;
		this.subdividedPolyhedron = polyhedron3DBis ;
		this.sourceFace = sourceFace ;
		
		this.correspondingReferenceFace = new HashMap<Face<Point_3>, Face<Point_3>>() ;
		this.computedDistanceField = new HashMap<Point_3, Double>();
		this.geodesicDistance = new GeodesicDistance(referencePolyhedron, sourceFace) ;
		this.subdivider = new LoopSubdivision(subdividedPolyhedron, correspondingReferenceFace);
		
		for(Face<Point_3> f : this.subdividedPolyhedron.facets){
			Face<Point_3> originalFace = null;
			for(Face<Point_3> of : this.referencePolyhedron.facets){
				
				if (ProjectUtils.isSameFace(f,of))
				{
					originalFace = of;
					break;
				}
			}
			
			this.correspondingReferenceFace.put(f, originalFace);
		}
		updateDistanceField();
	}
	
	public void subdivide() {
		this.subdivider.subdivide() ;
		updateDistanceField() ;
	}
	
	public void updateDistanceField() {
		for(Vertex<Point_3> v : this.subdividedPolyhedron.vertices){
			Face<Point_3> oldFace = this.correspondingReferenceFace.get(v.getHalfedge().getFace());
			computedDistanceField.put(v.getPoint(), geodesicDistance.distanceToSource(v.getPoint(), oldFace));
		}
	}

}
