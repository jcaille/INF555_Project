
import Jcg.geometry.Point_3;
import Jcg.polyhedron.Vertex;

public class Pair implements Comparable{
	
	Vertex<Point_3> v;
	double d;
	
	Pair(Vertex<Point_3> v, double d)
	{
		this.v = v;
		this.d = d;
	}

	@Override
	public int compareTo(Object arg0) {
		Pair y = (Pair) arg0;
		 if (this.d < y.d)
	     {
			 return -1;
	     }
	     if (this.d > y.d)
	    {
	    	 return 1;
	    }
	    return 0;
	}
}
