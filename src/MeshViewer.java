import processing.core.*;

import Jcg.geometry.*;

/**
 * A simple 3d viewer for visualizing surface meshes
 * 
 * @author Luca Castelli Aleardi (INF555, 2012)
 *
 */
public class MeshViewer extends PApplet {
	int limit = 10 ;
	
	SurfaceMesh mesh;
	ExactGeodesics eg;
	//String filename="OFF/high_genus.off";
//	String filename="OFF/sphere.off";
	//String filename="OFF/cube.off";
	String filename="OFF/torus_33.off";
//	String filename="OFF/tore.off";
	//String filename="OFF/tri_round_cube.off";
	//String filename="OFF/tri_hedra.off";
	//String filename="OFF/tri_horse.off";
	
	public void setup() {
		  size(800,600,P3D);
		  ArcBall arcball = new ArcBall(this);
		  
		  this.mesh=new SurfaceMesh(this, filename);
		  eg = new ExactGeodesics(this.mesh.polyhedron3D);
		  try {
			eg.compute(limit);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public void keyPressed(){
//		  switch(key) {
//		    case('s'):case('S'): this.limit++ ; try {
//				eg.compute(limit);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} break;
//		  }
//	}
		 
		public void draw() {
		  background(0);
		  //this.lights();
		  directionalLight(101, 204, 255, -1, 0, 0);
		  directionalLight(51, 102, 126, 0, -1, 0);
		  directionalLight(51, 102, 126, 0, 0, -1);
		  directionalLight(102, 50, 126, 1, 0, 0);
		  directionalLight(51, 50, 102, 0, 1, 0);
		  directionalLight(51, 50, 102, 0, 0, 1);
		 
		  translate(width/2.f,height/2.f,-1*height/2.f);
		  scale((float) 1.5) ;
		  this.strokeWeight((float) .3);
		  stroke(150,150,150);
		  
		  this.mesh.draw(eg.segments);

		  this.mesh.drawPoints(eg.points);
		}
		
		/**
		 * For running the PApplet as Java application
		 */
		public static void main(String args[]) {
			//PApplet pa=new MeshViewer();
			//pa.setSize(400, 400);
			PApplet.main(new String[] { "MeshViewer" });
		}

}
