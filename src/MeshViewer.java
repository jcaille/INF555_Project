import java.util.HashMap;

import processing.core.*;

import Jcg.geometry.*;
import Jcg.polyhedron.Vertex;

/**
 * A simple 3d viewer for visualizing surface meshes
 * 
 * @author Luca Castelli Aleardi (INF555, 2012)
 *
 */
public class MeshViewer extends PApplet {
	
	HashMap<Vertex<Point_3>, Double> distanceMap ;

	float xValue ;
	float yValue ;
	float zValue ;
	
	SurfaceMesh mesh;
	ExactDijkstra dj;
	ExactGeodesics eg;
//	String filename="OFF/high_genus.off";
//	String filename="OFF/sphere.off";
	//String filename="OFF/cube.off";
//	String filename="OFF/torus_33.off";
	//String filename="OFF/tore.off";
//	String filename="OFF/tri_round_cube.off";
	//String filename="OFF/tri_hedra.off";
	//String filename="OFF/tri_horse.off";
	String filename="OFF/tri_triceratops.off";
//	String filename="OFF/tri_gargoyle.off";


	public void setup() {
		size(800,600,P3D);
		ArcBall arcball = new ArcBall(this);

		xValue = 0 ;
		yValue = 0 ;
		zValue = 0 ;
		
		this.mesh=new SurfaceMesh(this, filename);
		dj = new ExactDijkstra(this.mesh.polyhedron3D);
		//eg = new ExactGeodesics(this.mesh.polyhedron3D);
		try {
			this.distanceMap = dj.compute();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void draw() {
		background(0);

		translate(width/2.f + xValue ,height/2.f + yValue,-1*height/2.f + zValue);
		scale((float) 1.5) ;
		this.strokeWeight((float) .3);
		stroke(150,150,150);

		this.mesh.drawWithDistanceField(this.distanceMap);
		
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
