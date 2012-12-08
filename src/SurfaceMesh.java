import java.util.HashMap;
import java.util.LinkedList;

import javax.media.j3d.Link;

import Jcg.geometry.*;
import Jcg.polyhedron.*;


/**
 * 
 * Class for rendering a surface triangle mesh
 * @author Luca Castelli Aleardi (INF555, 2012)
 *
 */
public class SurfaceMesh {

	double scaleFactor=50; // scaling factor: useful for 3d rendering
	MeshViewer view;
	public Polyhedron_3<Point_3> polyhedron3D; // triangle mesh

	/**
	 * Create a surface mesh from an OFF file
	 */	
	public SurfaceMesh(MeshViewer view, String filename) {
		this.view=view;
		
		// shared vertex representation of the mesh
		SharedVertexRepresentation sharedVertex=new SharedVertexRepresentation(filename);
		LoadMesh<Point_3> load3D=new LoadMesh<Point_3>();

		polyhedron3D=load3D.createTriangleMesh(sharedVertex.points,sharedVertex.faceDegrees,
				sharedVertex.faces,sharedVertex.sizeHalfedges);

		//System.out.println(polyhedron3D.verticesToString());   	
		//System.out.println(polyhedron3D.facesToString());
		polyhedron3D.isValid(false);

		this.scaleFactor=this.computeScaleFactor();
	}

	/**
	 * Draw a segment between two points
	 */	
	public void drawSegment(Point_3 p, Point_3 q) {
		float s=(float)this.scaleFactor;
		this.view.line(	(float)p.getX().doubleValue()*s, (float)p.getY().doubleValue()*s, 
				(float)p.getZ().doubleValue()*s, (float)q.getX().doubleValue()*s, 
				(float)q.getY().doubleValue()*s, (float)q.getZ().doubleValue()*s);
	}

	public void drawWindow(Window w, double maxDistance) {
		double meanDistance = w.sigma + (w.d0 + w.d1) / 2. ;
		int scaledColor = (int) (255 * (1 - meanDistance / maxDistance));
		this.view.stroke(scaledColor, 0, 255-scaledColor, 255) ;
		Point_3 p = w.getSegment().p;
		Point_3 q = w.getSegment().q;
		this.drawSegment(p, q);
	}
	
	/**
	 * Draw a triangle face
	 */	
	public void drawTriangle(Point_3 p, Point_3 q, Point_3 r) {
		float s=(float)this.scaleFactor;
		view.vertex( (float)(p.getX().doubleValue()*s), (float)(p.getY().doubleValue()*s), (float)(p.getZ().doubleValue()*s));
		view.vertex( (float)(q.getX().doubleValue()*s), (float)(q.getY().doubleValue()*s), (float)(q.getZ().doubleValue()*s));
		view.vertex( (float)(r.getX().doubleValue()*s), (float)(r.getY().doubleValue()*s), (float)(r.getZ().doubleValue()*s));
	}


	/**
	 * Draw the entire mesh
	 */
	public void draw() {
		this.drawAxis();

		view.beginShape(view.TRIANGLES);
		for(Face<Point_3> f: this.polyhedron3D.facets) {
			Halfedge<Point_3> e=f.getEdge();
			Point_3 p=e.vertex.getPoint();
			Point_3 q=e.getNext().vertex.getPoint();
			Point_3 r=e.getNext().getNext().vertex.getPoint();

			view.noStroke();
			view.fill(200,200,200,255); // color of the triangle
			this.drawTriangle(p, q, r); // draw a triangle face
		}
		view.endShape();

		view.strokeWeight(2); // line width (for edges)
		view.stroke(20);
		for(Halfedge<Point_3> e: this.polyhedron3D.halfedges) {
			Point_3 p=e.vertex.getPoint();
			Point_3 q=e.opposite.vertex.getPoint();

			this.drawSegment(p, q); // draw edge (p,q)
		}
		view.strokeWeight(1);
	}

	public void drawPoints(LinkedList<Point_3> list){
		for(Point_3 s: list) {
			this.drawVertex(s);// draw segment
		}
	}

	public void drawDistanceField(HashMap<Vertex<Point_3>, Double> map){
		double maxDistance = 0 ;
		for(Vertex<Point_3> v : map.keySet()){
			if(map.get(v) > maxDistance){
				maxDistance = map.get(v) ;
			}
		}
		for(Vertex<Point_3> v : this.polyhedron3D.vertices){
			int scaledColor = (int) (255 * (1 - map.get(v) / maxDistance));
			view.stroke(0, 255-scaledColor, scaledColor);
			this.drawVertex(v.getPoint()) ;
		}
	}

	public void drawWithDistanceField(HashMap<Vertex<Point_3>, Double> map){
		double maxDistance = 0 ;
		for(Vertex<Point_3> v : map.keySet()){
			if(map.get(v) > maxDistance){
				maxDistance = map.get(v) ;
			}
		}

		this.drawAxis();
		view.beginShape(view.TRIANGLES);
		for(Face<Point_3> f: this.polyhedron3D.facets) {
			int scaledColor = (int) (255 * (1 - map.get(f.getEdge().getVertex()) / maxDistance));
			Halfedge<Point_3> e=f.getEdge();
			Point_3 p=e.vertex.getPoint();
			Point_3 q=e.getNext().vertex.getPoint();
			Point_3 r=e.getNext().getNext().vertex.getPoint();

			view.noStroke();
			view.fill(255-scaledColor, 255-scaledColor,scaledColor,255); // color of the triangle
			this.drawTriangle(p, q, r); // draw a triangle face
		}
		view.endShape();

		view.strokeWeight(2); // line width (for edges)
		view.stroke(20);
		for(Halfedge<Point_3> e: this.polyhedron3D.halfedges) {
			Point_3 p=e.vertex.getPoint();
			Point_3 q=e.opposite.vertex.getPoint();
			this.view.stroke(0, 0, 0, 10) ;
			this.drawSegment(p, q); // draw edge (p,q)
		}
		view.strokeWeight(1);
	}
	
	public void drawWindowsField(HashMap<Halfedge<Point_3>, LinkedList<Window>> map){
		double maxDistance = 0 ;
		for(Halfedge<Point_3> h : map.keySet()){
			for( Window w : map.get(h)){
				maxDistance = w.getMaxDistanceToSource() > maxDistance ? w.getMaxDistanceToSource() : maxDistance ;
			}
		}

		this.drawAxis();
		view.beginShape(view.TRIANGLES);
		for(Face<Point_3> f: this.polyhedron3D.facets) {
			Halfedge<Point_3> e=f.getEdge();
			Point_3 p=e.vertex.getPoint();
			Point_3 q=e.getNext().vertex.getPoint();
			Point_3 r=e.getNext().getNext().vertex.getPoint();

			view.noStroke();
			view.fill(255, 255,255,255); // color of the triangle
			this.drawTriangle(p, q, r); // draw a triangle face
		}
		view.endShape();

		view.strokeWeight(2); // line width (for edges)
		view.stroke(20);
		for(Halfedge<Point_3> e: this.polyhedron3D.halfedges) {
			Point_3 p=e.vertex.getPoint();
			Point_3 q=e.opposite.vertex.getPoint();
			this.view.stroke(0, 0, 0, 10) ;
			this.drawSegment(p, q); // draw edge (p,q)
		}

		view.strokeWeight(10); // line width (for edges)
		for(Halfedge<Point_3> h : map.keySet()){
			for( Window w : map.get(h)){
				this.drawWindow(w, maxDistance);
			}
		}
		
		view.strokeWeight(1);
	}

	public void drawVertex(Point_3 p) {
		float s=(float)this.scaleFactor;
		float x1=(float)p.getX().doubleValue()*s;
		float y1=(float)p.getY().doubleValue()*s;
		float z1=(float)p.getZ().doubleValue()*s;

		view.translate(x1, y1, z1);
		view.sphere(s/50f);
		view.translate(-x1, -y1, -z1);
	}

	
	/**
	 * Draw the X, Y and Z axis
	 */
	public void drawAxis() {
		double s=1;
		Point_3 p000=new Point_3(0., 0., 0.);
		Point_3 p100=new Point_3(s, 0., 0.);
		Point_3 p010=new Point_3(0.,s, 0.);
		Point_3 p011=new Point_3(0., 0., s);

		drawSegment(p000, p100);
		drawSegment(p000, p010);
		drawSegment(p000, p011);
	}


	/**
	 * Return the value after truncation
	 */
	public static double round(double x, int precision) {
		return ((int)(x*precision)/(double)precision);
	}

	/**
	 * Compute the scale factor (depending on the max distance of the point set)
	 */
	public double computeScaleFactor() {
		if(this.polyhedron3D==null || this.polyhedron3D.vertices.size()<1)
			return 1;
		double maxDistance=0.;
		Point_3 origin=new Point_3(0., 0., 0.);
		for(Vertex<Point_3> v: this.polyhedron3D.vertices) {
			double distance=Math.sqrt(v.getPoint().squareDistance(origin).doubleValue());
			maxDistance=Math.max(maxDistance, distance);
		}
		return Math.sqrt(3)/maxDistance*150;
	}

}
