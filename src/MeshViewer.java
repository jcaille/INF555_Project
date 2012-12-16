import java.util.HashMap;

import processing.core.*;

import Jcg.geometry.*;
import Jcg.polyhedron.Face;
import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Vertex;

/**
 * A simple 3d viewer for visualizing surface meshes
 * 
 * @author Luca Castelli Aleardi (INF555, 2012)
 *
 */
public class MeshViewer extends PApplet {

	HashMap<Vertex<Point_3>, Double> distanceMap ;
	SurfaceMesh mesh, meshBis;
	Game meshGame ;

//		String filename="OFF/high_genus.off";
//		String filename="OFF/sphere.off";
//		String filename="OFF/cube.off";
//		String filename="OFF/torus_33.off";
//		String filename="OFF/tore.off";
//		String filename="OFF/tri_round_cube.off";
		String filename="OFF/tri_hedra.off";
//		String filename="OFF/tri_horse.off";
//		String filename="OFF/tri_triceratops.off";
//		String filename="OFF/tri_gargoyle.off";


	public void setup() {
		size(800,600,P3D);
		ArcBall arcball = new ArcBall(this);
		this.mesh=new SurfaceMesh(this, filename);
		this.meshBis=new SurfaceMesh(this, filename);
		this.meshGame = new Game(this.mesh.polyhedron3D,this.meshBis.polyhedron3D, this.mesh.polyhedron3D.facets.get(1));
		
		Face<Point_3> f = this.mesh.polyhedron3D.facets.get(3) ;
		Halfedge<Point_3> e = f.getEdge() ;
		
		Point_3 a = e.getVertex().getPoint();
		Point_3 b = e.getOpposite().getVertex().getPoint() ;
		Point_3 c = e.getNext().getVertex().getPoint() ;
		Point_3 d = ProjectUtils.barycenter(ProjectUtils.barycenter(a, b, 0.5), c, 0.5);
		Point_3 d1 = ProjectUtils.barycenter(a, b, 0.5);
		
		System.out.println(this.meshGame.geodesicDistance.distanceToSource(d, f));
		System.out.println(this.meshGame.geodesicDistance.distanceToSource(d1, f));
	}

	public void draw() {
		background(0);
		translate(width/2.f  ,height/2.f ,-1*height/2.f);
		scale((float) 1.5) ;
		this.mesh.drawPolyhedronWithDistanceField(this.meshGame.subdividedPolyhedron, this.meshGame.computedDistanceField);
		this.mesh.drawSegments(this.meshGame.geodesicDistance.segments);
	}

	public void keyPressed(){
		try{
			switch(key) {
			case('s') : case('S') : this.meshGame.subdivide() ; this.meshGame.subdividedPolyhedron.isValid(false) ; break ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * For running the PApplet as Java application
	 */
	public static void main(String args[]) {
		PApplet pa=new MeshViewer();
		pa.setSize(400, 400);
		PApplet.main(new String[] { "MeshViewer" });
	}


}
