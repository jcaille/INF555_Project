import java.util.HashMap;
import java.util.LinkedList;

import Jcg.geometry.Point_2;
import Jcg.geometry.Point_3;
import Jcg.geometry.Segment_3;
import Jcg.polyhedron.Face;
import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Polyhedron_3;
import Jcg.polyhedron.Vertex;


public class GeodesicDistance {

	public Polyhedron_3<Point_3> polyhedron3D;
	public DistanceField df;
	Point_3 source;
	Face<Point_3> sourceFace;
	public LinkedList<Segment_3> segments;

	public GeodesicDistance(Polyhedron_3<Point_3> polyhedron3D, Face<Point_3> sourceFace) {
		this.polyhedron3D=polyhedron3D;
		df = new DistanceField(polyhedron3D, sourceFace);
		try {
			long startingTime = System.currentTimeMillis(),endingTime;
			df.compute();
			endingTime = System.currentTimeMillis();
			System.out.println("Dur�e du calcul du champ de distance: "+(endingTime-startingTime)+"ms");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		segments = new LinkedList<Segment_3>();
		
		this.source = df.source;
		this.sourceFace = sourceFace;
	}
	
	public Double distanceToSource (Point_3 P, Face<Point_3> f){
		LinkedList<Halfedge<Point_3>> neighbouringEdges = new LinkedList<Halfedge<Point_3>>();
		LinkedList<Window> closeWindows = new LinkedList<Window>();

		//a, b and c are the three edges of our face ;
		Halfedge<Point_3> a = f.getEdge() ;
		Halfedge<Point_3> b = a.next ;
		Halfedge<Point_3> c = b.next ;
		
		boolean pIsVertex = (a.vertex.getPoint().equals(P)) || (b.getVertex().getPoint().equals(P)) || (c.getVertex().getPoint().equals(P));
		boolean pIsOnA = ProjectUtils.sumMatch(a.getVertex().getPoint(), a.getOpposite().getVertex().getPoint(), P);
		boolean pIsOnB = ProjectUtils.sumMatch(b.getVertex().getPoint(), b.getOpposite().getVertex().getPoint(), P);
		boolean pIsOnC = ProjectUtils.sumMatch(c.getVertex().getPoint(), c.getOpposite().getVertex().getPoint(), P);

		
		//Is P a vertex?

		if(pIsVertex)	//if P is a vertex, we directly get the distance
		{
			Halfedge<Point_3> h = null , next ;
			if(a.getVertex().getPoint().equals(P)){
				h = a ;
			} else if (b.getVertex().getPoint().equals(P)){
				h = b ;
			} else if (c.getVertex().getPoint().equals(P)){
				h = c ;
			}
			
			neighbouringEdges.push(h);

			next = h.getNext().getOpposite();

			while(next != h)
			{
				neighbouringEdges.push(next);
				next = next.getNext().getOpposite();
			}
			
			//We use them to get the distance
			Double distance = -1.;
			for(Halfedge<Point_3> n : neighbouringEdges)
			{
				if(df.computedWindows.containsKey(n)){
					for(Window w : df.computedWindows.get(n))
					{
						if(w.b0 == 0 && ((distance < 0) || (w.d0 + w.sigma < distance) ))
							distance = w.d0 + w.sigma;
					}
				}
			
				if(df.computedWindows.containsKey(n.opposite)){
					for(Window w : df.computedWindows.get(n.opposite))
					{
						if(ProjectUtils.equals(w.b1, Window.edgeLength(w.edge)) && ((distance < 0) || (w.d1 + w.sigma < distance) ))
							distance = w.d1 + w.sigma;
					}
				}
			}
			return(distance);
		}
		
		if(pIsOnA || pIsOnB || pIsOnC){
			Halfedge<Point_3> edge = null ;
			if(pIsOnA){ edge = a; } 
			if(pIsOnB){ edge = b; } 
			if(pIsOnC){ edge = c; } 
			
			double distance = -1 ;
			if(df.computedWindows.containsKey(edge)){
				for(Window w : df.computedWindows.get(edge)){
					if(w.containsPoint(P)){
						if(w.distanceOfPointOnEdgeToSource(P) < distance || distance < 0){
							distance = w.distanceOfPointOnEdgeToSource(P);
						}
					}
				}
			}
			
			if(df.computedWindows.containsKey(edge.opposite)){
				for(Window w : df.computedWindows.get(edge.opposite)){
					if(w.containsPoint(P)){
						if(w.distanceOfPointOnEdgeToSource(P) < distance || distance < 0){
							distance = w.distanceOfPointOnEdgeToSource(P);
						}
					}
				}
			}
			
			return distance ;
		}
		
		//Otherwise, we consider all windows on the neighbouring edges,
		//and we minimise the distance from the source through them
		//If the point is on an edge, we will add the edges of the opposite face as well
		
		neighbouringEdges.add(a) ;
		neighbouringEdges.add(b) ;
		neighbouringEdges.add(c) ;
		
		double distance = 1000000000.0 ;
		for(Halfedge<Point_3> e : neighbouringEdges){
			if(this.df.computedWindows.containsKey(e)){
				for(Window w : this.df.computedWindows.get(e)){
					double currentDistance = w.minDistanceThroughWindow(P) ;
					if(currentDistance < distance && currentDistance >= 0){
						distance = currentDistance ;
					}
				}
			}
			
			if(this.df.computedWindows.containsKey(e.opposite)){
				for(Window w : this.df.computedWindows.get(e.opposite)){
					double currentDistance = w.minDistanceThroughWindow(P) ;
					if(currentDistance < distance && currentDistance >= 0){
						distance = currentDistance ;
					}
				}
			}
		}
		
		return distance ;
	}

	public LinkedList<Point_3> pathToSource(Point_3 P, Face<Point_3> f){
		//If we already have the source, we return it
		if(ProjectUtils.equals(P.distanceFrom(source).doubleValue(), 0))
		{
			LinkedList<Point_3> result = new LinkedList<Point_3>();
			result.add(source);
			return(result);
		}
		
		//If we do not have the source, but are in its face,
		//we can go to it in a straight line
		if(f == sourceFace)
		{
			LinkedList<Point_3> result = new LinkedList<Point_3>();
			result.add(source);
			result.add(P);
			return(result);
		}
		
		//If it is more complicated, we call the function recursively on the most promising point
		//and add the current point at the end of the resulting list
		
		//First of all: is P a vertex
		LinkedList<Halfedge<Point_3>> neighbouringEdges = new LinkedList<Halfedge<Point_3>>();
		LinkedList<Window> closeWindows = new LinkedList<Window>();

		Halfedge<Point_3> h = f.getEdge(), next=h.getNext(), last = null;

		neighbouringEdges.push(h);

		while(next != h)
		{
			if(next.getVertex().getPoint().equals(P))
			{
				last = next;
				break;
			}
			neighbouringEdges.push(next);
			next = next.getNext();
		}

		if(next.getVertex().getPoint().equals(P))
		{
			last = next;
		}

		if(last != null)	//if P is a vertex, we search how we got to p in the shortest way(minimal d0 or d1 different from 0)
		{
			//We determine the halfedges pointing to P
			h = last;
			neighbouringEdges.push(h);

			next = h.getNext().getOpposite();

			while(next != h)
			{
				neighbouringEdges.push(next);
				next = next.getNext().getOpposite();
			}
			
			
			//We use them to get the distance
			Double distance = -1.;
			Window bestWindow = null;
			Halfedge<Point_3> oppositeEdge = null;
			for(Halfedge<Point_3> n : neighbouringEdges)
			{
				if(df.computedWindows.containsKey(n.opposite)){
					for(Window w : df.computedWindows.get(n))
					{
						if(w.b0 == 0 && w.d0 != 0 && ((distance < 0) || (w.d0 + w.sigma < distance) ))
						{
							distance = w.d0 + w.sigma;
							bestWindow = w;
							oppositeEdge = w.edge.getOpposite().getNext();
						}
					}
				}
				
				if(df.computedWindows.containsKey(n.opposite)){
					for(Window w : df.computedWindows.get(n.opposite))
					{
						if(ProjectUtils.equals(w.b1, Window.edgeLength(w.edge)) && w.d1 != 0 && ((distance < 0) || (w.d1 + w.sigma < distance) ))
						{
							distance = w.d1 + w.sigma;
							bestWindow = w;
							oppositeEdge = w.edge.getOpposite().getNext().getNext();
						}
					}
				}
				
			}
			
			//We compute the position of P in 2D, relatively to the opposite edge
			Point_2 P_2D = ProjectUtils.getThirdTriangleVertex(0, Window.edgeLength(oppositeEdge), Window.edgeLength(oppositeEdge.getNext()), Window.edgeLength(oppositeEdge.getPrev()), 1);
			//We use it to determine in 2D the coordinates of the new point
			Point_2 newPoint2D = ProjectUtils.intersectLines(new Point_2(0,0), new Point_2(0,Window.edgeLength(oppositeEdge)), P_2D, bestWindow.getSourceInPlane());
			//And we deduce the new point in 3D
			Point_3 newPoint = ProjectUtils.barycenter(oppositeEdge.getVertex().getPoint(), oppositeEdge.getOpposite().getVertex().getPoint(), newPoint2D.x);
			
			//We now have the new point to use recursion
			LinkedList<Point_3> result = pathToSource(newPoint,oppositeEdge.getOpposite().getFace());
			result.add(P);
			return(result);
		}
		
		//Otherwise, we consider all windows on the neighbouring edges, and we search how to minimise
		//the distance from the source through them
		
		for(Halfedge<Point_3> n : neighbouringEdges)
		{
			if(df.computedWindows.containsKey(n)){
				closeWindows.addAll(df.computedWindows.get(n));
			}
		}
		
		Double distance = -1.;
		Window bestWindow = null;
		Point_3 newPoint = null;
		
		for(Window w : closeWindows)
		{
			//We want to compute the shortest distance to the source through the window
			double minThroughWindow = 0;
			
			//We start by transforming P into a 2D point in the plane define by P and the edge
			Point_3 P1 = w.edge.getOpposite().getVertex().getPoint();
			Point_3 P0 = w.edge.getVertex().getPoint();
			
			Point_3 P0P1 = new Point_3(P1.x-P0.x, P1.y-P0.y, P1.z-P0.z);
			double nP0P1 = Math.sqrt(P0P1.x*P0P1.x + P0P1.y*P0P1.y + P0P1.y*P0P1.y);
			P0P1.x /= nP0P1;
			P0P1.y /= nP0P1;
			P0P1.z /= nP0P1;
			
			Point_2 P_2D = new Point_2();
			
			P_2D.x = (P.x-P0.x)*P0P1.x+(P.y-P0.y)*P0P1.y+(P.z-P0.z)*P0P1.z;
			P_2D.y = Math.sqrt((P.x - P_2D.x*P0P1.x)*(P.x - P_2D.x*P0P1.x) + (P.y - P_2D.x*P0P1.y)*(P.y - P_2D.x*P0P1.y) + (P.z - P_2D.x*P0P1.z)*(P.z - P_2D.x*P0P1.z));
			
			//Now, can we joint P and the pseudo-source with a straight line?
			Point_2 S = w.getSourceInPlane();
			Point_3 passagePoint;
			
			if(Math.acos((P_2D.x-w.b0)/Math.sqrt((P_2D.x-w.b0)*(P_2D.x-w.b0)+P_2D.y*P_2D.y)) + Math.acos((S.x-w.b0)/Math.sqrt((S.x-w.b0)*(S.x-w.b0)+S.y*S.y)) > Math.PI)
			{
				//we sum the angles (B0P,B0B1) and (B0S,B0B1). If the result is bigger than pi,
				//then S is two far beyond B0 to be reached through the window in a straight line
				minThroughWindow = Math.sqrt( (P_2D.x - w.b0)*(P_2D.x - w.b0) + P_2D.y*P_2D.y ) + Math.sqrt( (S.x - w.b0)*(S.x - w.b0) + S.y*S.y );
				passagePoint = ProjectUtils.barycenter(P0, P1, w.b0);
			}
			else if(Math.acos(-(P_2D.x-w.b1)/Math.sqrt((P_2D.x-w.b1)*(P_2D.x-w.b1)+P_2D.y*P_2D.y)) + Math.acos(-(S.x-w.b1)/Math.sqrt((S.x-w.b1)*(S.x-w.b1)+S.y*S.y)) > Math.PI)
			{
				//we sum the angles (B1P,B0B1) and (B1S,B0B1). If the result is bigger than pi,
				//then S is two far beyond B1 to be reached through the window in a straight line
				minThroughWindow = Math.sqrt( (P_2D.x - w.b1)*(P_2D.x - w.b1) + P_2D.y*P_2D.y ) + Math.sqrt( (S.x - w.b1)*(S.x - w.b1) + S.y*S.y );
				passagePoint = ProjectUtils.barycenter(P0, P1, w.b1);
			}
			else
			{
				//we can join P and S through the window
				minThroughWindow = Math.sqrt( (S.x-P_2D.x)*(S.x-P_2D.x) + (S.y-P_2D.y)*(S.y-P_2D.y) );
				Point_2 passagePoint2D = ProjectUtils.intersectLines(S, P_2D, new Point_2(0,w.b0), new Point_2(0,w.b1));
				passagePoint = ProjectUtils.barycenter(P0, P1, passagePoint2D.x);
			}
			
			//We add the distance from the pseudo-source to the source
			minThroughWindow += w.sigma;
			
			if(distance < 0 || minThroughWindow < distance)
			{
				distance = minThroughWindow;
				bestWindow = w;
				newPoint = passagePoint;
			}
		}
		
		LinkedList<Point_3> result = pathToSource(newPoint,bestWindow.edge.getOpposite().getFace());
		result.add(P);
		return(result);
	}

}
