import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import com.sun.tools.apt.util.Bark;

import Jcg.geometry.*;
import Jcg.polyhedron.*;

public class ExactGeodesics {
	
	public Polyhedron_3<Point_3> polyhedron3D;
	public LinkedList<Segment_3> segments;
	public LinkedList<Point_3> points ;
	
	public HashMap<Halfedge<Point_3>, LinkedList<Window>> computedWindows ;
	public PriorityQueue<Window> windowsToPropagate ;
	
	public ExactGeodesics(Polyhedron_3<Point_3> polyhedron3D) {
		this.polyhedron3D=polyhedron3D;
		segments = new LinkedList<Segment_3>();
		points = new LinkedList<Point_3>();
		
		computedWindows = new HashMap<Halfedge<Point_3>, LinkedList<Window>>() ;
		windowsToPropagate = new PriorityQueue<Window>(this.polyhedron3D.sizeOfHalfedges(), new WindowComparator());
	}
	
	public PairOfLists mergeTwoWindows(Window w1, Window w2){
		//Take two windows that are not compatible
		//Merge the two windows, and put the new windows into two LinkedLists
		//(the first one for w1 and to secund one for w2)
		
		Window leftWindow, rightWindow;
		LinkedList<Window> leftList = new LinkedList<Window>(), rightList = new LinkedList<Window>();
		boolean w1Left;
		
		//leftWindow is the window with the lowest b0
		if(w1.b0 < w2.b0)
		{
			leftWindow = w1;
			rightWindow = w2;
			w1Left = true;
		}
		else
		{
			leftWindow = w2;
			rightWindow = w1;
			w1Left = false;
		}
		
		/*if(!ProjectUtils.equals(leftWindow.b0, rightWindow.b0))
		{
			//Add a window from leftWindow.b0 to rightWindow.b0
			//leftWindow = new Window, with b0 = rightWindow.b0
		}
		
		if(leftWindow.getMinDistanceToSource() > rightWindow.getMaxDistanceToSource())
		{
			//If the right window is better than the left one 
			rightList.add(rightWindow);
		}
		else if(leftWindow.getMaxDistanceToSource() < rightWindow.getMinDistanceToSource())
		{
			leftList.add(leftWindow);
		}
		else
		{
			
		}*/
		
		
		
		
		//We return the lists
		if(w1Left)
		{
			return new PairOfLists(leftList, rightList);
		}
		else
		{
			return new PairOfLists(rightList, leftList);
		}
	}
	
	public void merge(LinkedList<Window> windowsToMerge){
		
		//Before anything, we remove the empty windows
		LinkedList<Window> newWindowsToMerge = new LinkedList<Window>() ;
		for (Window newWindow : windowsToMerge){
			if(!ProjectUtils.equals(newWindow.b0, newWindow.b1))
			{
				newWindowsToMerge.push(newWindow);
			}
		}
		windowsToMerge = newWindowsToMerge;
		
		LinkedList<Window> existingWindows = this.computedWindows.get(windowsToMerge.get(0).edge);
		LinkedList<Window> resultingOldWindows = new LinkedList<Window>() ;
		LinkedList<Window> newExistingWindows = new LinkedList<Window>();
		
		for (Window oldWindow : existingWindows){
			//We consider each old window individually
			resultingOldWindows.clear();
			resultingOldWindows.push(oldWindow);
			
			boolean needsPropagation = windowsToPropagate.remove(oldWindow);
			
			newWindowsToMerge.clear();
			
			for (Window newWindow : windowsToMerge){
				//We look how to merge each new window with the old one
				
				LinkedList<Window> newResultingOldWindows = new LinkedList<Window>();
				
				for(Window w : resultingOldWindows){
					if(!w.isCompatibleWith(newWindow)){
						//If necessary, we create new lists for old and new windows
						PairOfLists pair = mergeTwoWindows(w, newWindow);
						newResultingOldWindows.addAll(pair.first);
						newWindowsToMerge.addAll(pair.secund);
					}
					else
					{
						//Or we can just keep the windows as is if they are compatible
						newResultingOldWindows.add(w);
						newWindowsToMerge.add(newWindow);
					}
				}
				resultingOldWindows = newResultingOldWindows;
			}
			windowsToMerge = newWindowsToMerge;
			
			newExistingWindows.addAll(resultingOldWindows);
			
			if(needsPropagation)
			{
				windowsToPropagate.addAll(resultingOldWindows);
			}
		}
		windowsToPropagate.addAll(windowsToMerge);
		newExistingWindows.addAll(windowsToMerge);
		existingWindows = newExistingWindows;
	}
	
	public void compute() throws Exception
	{
		Face<Point_3> f = this.polyhedron3D.facets.get(5) ;
		Point_3 a = f.getEdge().getVertex().getPoint() ;
		Point_3 b = f.getEdge().getOpposite().getVertex().getPoint() ;
		Point_3 c = f.getEdge().getNext().getVertex().getPoint() ;
		
		Point_3 X = Window.barycenter(b, a, 0.55) ;
		Point_3 Y = Window.barycenter(b, a, .66) ;
				
		Point_3 source = Window.barycenter(Window.barycenter(a, b, 0.5), c, 0.5) ;
		double b0 = (Double) b.distanceFrom(X) ; System.out.println(b0) ;
		double b1 = (Double) b.distanceFrom(Y) ; System.out.println(b1) ;
		double d0 = (Double) source.distanceFrom(X) ;
		double d1 = (Double) source.distanceFrom(Y) ;
		
		Window myWindow = new Window(f.getEdge().getOpposite(), b0, b1, d0, d1, -1);
		
		this.points.add(source) ;
				
		LinkedList<Window> windowsToDraw = new LinkedList<Window>() ;
		LinkedList<Window> windowToPropagate = new LinkedList<Window>() ;
		
		windowToPropagate.add(myWindow);

		int i = 0 ;
		while(!windowToPropagate.isEmpty() && i < 100000){
			i++;
			Window w = windowToPropagate.poll() ;
			windowsToDraw.add(w) ;
			LinkedList<Window> newWindows  = w.propagate();
			windowToPropagate.addAll(newWindows) ;
		}

		for(Window w : windowsToDraw){
			this.segments.add(w.getSegment()) ;
		}
	}
}
