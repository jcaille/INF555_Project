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
	SurfaceMesh mesh;
	Game meshGame ;
	
//	String filename="OFF/high_genus.off";
	String filename="OFF/sphere.off";
//	String filename="OFF/cube.off";
//	String filename="OFF/torus_33.off";
//	String filename="OFF/tore.off";
//	String filename="OFF/tri_round_cube.off";
//	String filename="OFF/tri_hedra.off";
//	String filename="OFF/tri_horse.off";
//	String filename="OFF/tri_triceratops.off";
//	String filename="OFF/tri_gargoyle.off";


	public void setup() {
		size(800,600,P3D);
		ArcBall arcball = new ArcBall(this);
		this.mesh=new SurfaceMesh(this, filename);
		this.meshGame = new Game(this.mesh.polyhedron3D, this.mesh.polyhedron3D.facets.get(1));
	}

	public void draw() {
		background(0);

		scale((float) 1.5) ;
		this.strokeWeight((float) .3);
		stroke(150,150,150);
	}
	
	public void keyPressed(){
		try{
		  switch(key) {

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
