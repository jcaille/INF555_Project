import java.util.HashMap;
import java.util.LinkedList;

import Jcg.geometry.Point_2;
import Jcg.geometry.Point_3;
import Jcg.polyhedron.Face;
import Jcg.polyhedron.Halfedge;
import Jcg.polyhedron.Polyhedron_3;
import Jcg.polyhedron.Vertex;


public class GeodesicDistance {

	public Polyhedron_3<Point_3> polyhedron3D;
	public ExactGeodesics eg;
	Point_3 source;
	Face<Point_3> sourceFace;

	public GeodesicDistance(Polyhedron_3<Point_3> polyhedron3D, Point_3 source, Face<Point_3> sourceFace) {
		this.polyhedron3D=polyhedron3D;
		eg = new ExactGeodesics(polyhedron3D);
		try {
			eg.compute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.source = source;
		this.sourceFace = sourceFace;
	}

	public Double distanceToSource (Point_3 P, Face<Point_3> f){
		LinkedList<Halfedge<Point_3>> neighbouringEdges = new LinkedList<Halfedge<Point_3>>();
		LinkedList<Window> closeWindows = new LinkedList<Window>();

		//Is P a vertex?
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

		if(last != null)	//if P is a vertex, we directly get the distance
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
			for(Halfedge<Point_3> n : neighbouringEdges)
			{
				for(Window w : eg.computedWindows.get(n))
				{
					if(w.b0 == 0 && ((distance < 0) || (w.d0 + w.sigma < distance) ))
						distance = w.d0 + w.sigma;
				}
				for(Window w : eg.computedWindows.get(n.opposite))
				{
					if(ProjectUtils.equals(w.b1, Window.edgeLength(w.edge)) && ((distance < 0) || (w.d1 + w.sigma < distance) ))
						distance = w.d1 + w.sigma;
				}
			}
			return(distance);
		}
		
		//Otherwise, we consider all windows on the neighbouring edges, and we minimise the distance
		//from the source through them
		
		for(Halfedge<Point_3> n : neighbouringEdges)
		{
			closeWindows.addAll(eg.computedWindows.get(n));
		}
		
		Double distance = -1.;
		
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
			
			if(Math.acos((P_2D.x-w.b0)/Math.sqrt((P_2D.x-w.b0)*(P_2D.x-w.b0)+P_2D.y*P_2D.y)) + Math.acos((S.x-w.b0)/Math.sqrt((S.x-w.b0)*(S.x-w.b0)+S.y*S.y)) > Math.PI)
			{
				//we sum the angles (B0P,B0B1) and (B0S,B0B1). If the result is bigger than pi,
				//then S is two far beyond P0 to be reached through the window in a straight line
				minThroughWindow = Math.sqrt( (P_2D.x - w.b0)*(P_2D.x - w.b0) + P_2D.y*P_2D.y ) + Math.sqrt( (S.x - w.b0)*(S.x - w.b0) + S.y*S.y );
			}
			else if(Math.acos(-(P_2D.x-w.b1)/Math.sqrt((P_2D.x-w.b1)*(P_2D.x-w.b1)+P_2D.y*P_2D.y)) + Math.acos(-(S.x-w.b1)/Math.sqrt((S.x-w.b1)*(S.x-w.b1)+S.y*S.y)) > Math.PI)
			{
				//we sum the angles (B1P,B0B1) and (B1S,B0B1). If the result is bigger than pi,
				//then S is two far beyond P1 to be reached through the window in a straight line
				minThroughWindow = Math.sqrt( (P_2D.x - w.b1)*(P_2D.x - w.b1) + P_2D.y*P_2D.y ) + Math.sqrt( (S.x - w.b1)*(S.x - w.b1) + S.y*S.y );
			}
			else
			{
				//we can join P and S through the window
				minThroughWindow = Math.sqrt( (S.x-P_2D.x)*(S.x-P_2D.x) + (S.y-P_2D.y)*(S.y-P_2D.y) );
			}
			
			//We add the distance from the pseudo-source to the source
			minThroughWindow += w.sigma;
			
			if(distance < 0 || minThroughWindow < distance)
				distance = minThroughWindow;
		}

		return(distance);
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
				for(Window w : eg.computedWindows.get(n))
				{
					if(w.b0 == 0 && w.d0 != 0 && ((distance < 0) || (w.d0 + w.sigma < distance) ))
					{
						distance = w.d0 + w.sigma;
						bestWindow = w;
						oppositeEdge = w.edge.getOpposite().getNext();
					}
				}
				for(Window w : eg.computedWindows.get(n.opposite))
				{
					if(ProjectUtils.equals(w.b1, Window.edgeLength(w.edge)) && w.d1 != 0 && ((distance < 0) || (w.d1 + w.sigma < distance) ))
					{
						distance = w.d1 + w.sigma;
						bestWindow = w;
						oppositeEdge = w.edge.getOpposite().getNext().getNext();
					}
				}
			}
			
			//We compute the position of P in 2D, relatively to the opposite edge
			Point_2 P_2D = bestWindow.getThirdTriangleVertex(0, Window.edgeLength(oppositeEdge), Window.edgeLength(oppositeEdge.getNext()), Window.edgeLength(oppositeEdge.getPrev()), 1);
			//We use it to determine in 2D the coordinates of the new point
			Point_2 newPoint2D = bestWindow.intersectLines(new Point_2(0,0), new Point_2(0,Window.edgeLength(oppositeEdge)), P_2D, bestWindow.getSourceInPlane());
			//And we deduce the new point in 3D
			Point_3 newPoint = Window.barycenter(oppositeEdge.getVertex().getPoint(), oppositeEdge.getOpposite().getVertex().getPoint(), newPoint2D.x);
			
			//We now have the new point to use recursion
			LinkedList<Point_3> result = pathToSource(newPoint,oppositeEdge.getOpposite().getFace());
			result.add(P);
			return(result);
		}
		
		//Otherwise, we consider all windows on the neighbouring edges, and we search how to minimise
		//the distance from the source through them
		
		for(Halfedge<Point_3> n : neighbouringEdges)
		{
			closeWindows.addAll(eg.computedWindows.get(n));
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
				passagePoint = Window.barycenter(P0, P1, w.b0);
			}
			else if(Math.acos(-(P_2D.x-w.b1)/Math.sqrt((P_2D.x-w.b1)*(P_2D.x-w.b1)+P_2D.y*P_2D.y)) + Math.acos(-(S.x-w.b1)/Math.sqrt((S.x-w.b1)*(S.x-w.b1)+S.y*S.y)) > Math.PI)
			{
				//we sum the angles (B1P,B0B1) and (B1S,B0B1). If the result is bigger than pi,
				//then S is two far beyond B1 to be reached through the window in a straight line
				minThroughWindow = Math.sqrt( (P_2D.x - w.b1)*(P_2D.x - w.b1) + P_2D.y*P_2D.y ) + Math.sqrt( (S.x - w.b1)*(S.x - w.b1) + S.y*S.y );
				passagePoint = Window.barycenter(P0, P1, w.b1);
			}
			else
			{
				//we can join P and S through the window
				minThroughWindow = Math.sqrt( (S.x-P_2D.x)*(S.x-P_2D.x) + (S.y-P_2D.y)*(S.y-P_2D.y) );
				Point_2 passagePoint2D = w.intersectLines(S, P_2D, new Point_2(0,w.b0), new Point_2(0,w.b1));
				passagePoint = Window.barycenter(P0, P1, passagePoint2D.x);
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
