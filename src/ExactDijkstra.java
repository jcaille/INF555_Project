import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import com.sun.tools.apt.util.Bark;

import Jcg.geometry.*;
import Jcg.polyhedron.*;

public class ExactDijkstra {

	public Polyhedron_3<Point_3> polyhedron3D;
	public LinkedList<Segment_3> segments;
	public LinkedList<Point_3> points ;

	public ExactDijkstra(Polyhedron_3<Point_3> polyhedron3D) {
		this.polyhedron3D=polyhedron3D;
		segments = new LinkedList<Segment_3>();
		points = new LinkedList<Point_3>();
	}


	public HashMap<Vertex<Point_3>, Double> compute() throws Exception
	{
		Face<Point_3> f = this.polyhedron3D.facets.get(5) ;
		/*Point_3 a = f.getEdge().getVertex().getPoint() ;
		Point_3 b = f.getEdge().getOpposite().getVertex().getPoint() ;
		Point_3 c = f.getEdge().getNext().getVertex().getPoint() ;

		Point_3 source = Window.barycenter(Window.barycenter(a, b, 0.5), c, 0.5) ;*/

		HashMap<Vertex<Point_3>,Double> map = dijkstra(f.getEdge().getVertex());
		return map;
	}

	public HashMap<Vertex<Point_3>,Double> dijkstra(Vertex<Point_3> s)
	{
		HashMap<Vertex<Point_3>,Double> map = new HashMap<Vertex<Point_3>,Double>();
		PriorityQueue<Pair> frontier = new PriorityQueue<Pair>();
		map.put(s,0.);
		frontier.add(new Pair(s,0));

		while(map.size() < this.polyhedron3D.vertices.size())
		{
			explore(map,frontier);
		}

		return(map);
	}	

	public void explore(HashMap<Vertex<Point_3>,Double> map, PriorityQueue<Pair> frontier)
	{
		Pair start = frontier.poll();
		LinkedList<Vertex<Point_3>> neigh = neighbours(start.v);

		for(Vertex<Point_3> n:neigh)
		{
			double dist = start.d + Math.sqrt( start.v.getPoint().minus( n.getPoint() ).squaredLength().doubleValue() );
			if(!map.containsKey(n) || map.get(n).doubleValue() > dist)
			{
				map.put(n, dist);
				frontier.add(new Pair(n,dist));
			}
		}
	}

	public LinkedList<Vertex<Point_3>> neighbours(Vertex<Point_3> u)
	{
		LinkedList<Vertex<Point_3>> list = new LinkedList<Vertex<Point_3>>();
		Halfedge<Point_3> h = u.getHalfedge().getOpposite();
		Vertex<Point_3> v = h.getVertex(), next;

		list.add(v);
		h = h.getOpposite().getNext();
		next = h.getVertex();

		while(next != v)
		{
			list.add(next);
			h = h.getOpposite().getNext();
			next = h.getVertex();
		}

		return(list);
	}

}
