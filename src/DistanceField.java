import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import com.sun.tools.apt.util.Bark;

import Jcg.geometry.*;
import Jcg.polyhedron.*;

public class DistanceField {
	
	public Polyhedron_3<Point_3> polyhedron3D;
	
	public HashMap<Halfedge<Point_3>, LinkedList<Window>> computedWindows ;
	public PriorityQueue<Window> windowsToPropagate ;
	public Point_3 source ;
	public Face<Point_3> sourceFace ;
	
	public DistanceField(Polyhedron_3<Point_3> polyhedron3D) {
		this.polyhedron3D=polyhedron3D;
		computedWindows = new HashMap<Halfedge<Point_3>, LinkedList<Window>>() ;
		windowsToPropagate = new PriorityQueue<Window>(this.polyhedron3D.sizeOfHalfedges(), new WindowComparator());
		this.sourceFace = this.polyhedron3D.facets.get(500) ;
		this.getFirstWindow();
	}
	
	public DistanceField(Polyhedron_3<Point_3> polyhedron3D, Face<Point_3> sourceFace) {
		this.polyhedron3D=polyhedron3D;
		computedWindows = new HashMap<Halfedge<Point_3>, LinkedList<Window>>() ;
		windowsToPropagate = new PriorityQueue<Window>(this.polyhedron3D.sizeOfHalfedges(), new WindowComparator());
		this.sourceFace = sourceFace ;
		this.getFirstWindow();
	}
	
	public void getFirstWindow(){
		Face<Point_3> f = this.sourceFace ;
		Point_3 source = f.getEdge().getVertex().getPoint() ;
		Halfedge<Point_3> e = f.getEdge() ;
		do{
			double b0 = 0 ;
			double b1 = (Double) source.distanceFrom(e.opposite.getVertex().getPoint());
			double d0 = 0 ;
			double d1 = b1 ;
			Window myWindow = new Window(e, b0, b1, d0, d1, 1, 0);
			LinkedList<Window> l = new LinkedList<Window>();
			l.add(myWindow);
			this.computedWindows.put(e, l);
			this.windowsToPropagate.add(myWindow);
			
			e = e.next.opposite ;
		} while (e != f.getEdge());
	}
	
	public PairOfLists mergeTwoWindows(Window w1, Window w2) throws Exception{
		//Take two windows that are not compatible
		//Merge the two windows, and put the new windows into two LinkedLists
		//(the first one for w1 and to second one for w2)
		//We assume that w1 has priority over w2 if they are equals.
		
		Window leftWindow, rightWindow;
		LinkedList<Window> leftList = new LinkedList<Window>(), rightList = new LinkedList<Window>();
		boolean w1Left;
		
		if(w1.equals(w2)){
			leftList.add(w1);
			return new PairOfLists(leftList, rightList);
		}

		//leftWindow is the window with the lowest b0
		if(w1.b0 < w2.b0){
			leftWindow = w1;
			rightWindow = w2;
			w1Left = true;
		} else {
			leftWindow = w2;
			rightWindow = w1;
			w1Left = false;
		}
		
		double leftOverlap = rightWindow.b0 ;
		double rightOverlap = Math.min(leftWindow.b1, rightWindow.b1);
		
		//Magic
		if (((leftWindow.distanceOnEdge(leftOverlap) - rightWindow.distanceOnEdge(leftOverlap)) * 
				(leftWindow.distanceOnEdge(rightOverlap) - rightWindow.distanceOnEdge(rightOverlap))) > 0){
			//One window dominates the other one over all the overlap.
			//We make a test to determine this window
			if( leftWindow.distanceOnEdge(leftOverlap) < rightWindow.distanceOnEdge(rightOverlap)){
				//Leftwindow dominates !
				leftList.add(leftWindow);
				
				rightWindow.d0 = rightWindow.distanceOnEdge(rightOverlap);
				rightWindow.b0 = rightOverlap ;
				rightList.add(rightWindow);
			} else {
				//Rightwindow dominates !
				rightList.add(rightWindow);
				
				if(leftWindow.b1 > rightWindow.b1){
					leftList.add(new Window(leftWindow,rightOverlap,leftWindow.b1));
				}
				
				leftWindow.d1 = leftWindow.distanceOnEdge(leftOverlap);
				leftWindow.b1 = leftOverlap ;
				leftList.add(leftWindow);
			}
		} else {
			Point_2 source0 = leftWindow.getSourceInPlane() ;
			Point_2 source1 = rightWindow.getSourceInPlane();
			
			double alpha = source1.x - source0.x ;
			double beta = rightWindow.sigma - leftWindow.sigma ;
			double gamma = source0.x * source0.x + source0.y * source0.y
					- source1.x * source1.x - source1.y * source1.y
					- beta * beta ;
			
			double A = (alpha * alpha - beta*beta);
			double B = (gamma * alpha + 2 * source1.x * beta * beta);
			double C = (0.25 * gamma * gamma - (source1.x * source1.x - source1.y * source1.y) * beta * beta );
			
			double delta = B * B - 4 * A * C;
			
			if( delta < 0){
				System.out.println("Delta is not positive");
				throw new Exception("Delta is not positive");
			}
			
			double px = 0 ;
			if (delta == 0){
				px = -B / (2*A) ;
			} else {
				double px0 = (-B + Math.sqrt(delta)) / (2*A) ;
				double px1 = (-B - Math.sqrt(delta)) / (2*A) ;
				
				if( leftOverlap < px0  && px0 < rightOverlap ){
					px = px0 ;
				} else if(leftOverlap < px1 && px1 < rightOverlap) {
					px = px1 ;
				} else {
					System.out.println("px is not defined");
					throw new Exception("px is not defined");
				}
			}
			
			if(leftWindow.distanceOnEdge(leftOverlap) > rightWindow.distanceOnEdge(leftOverlap)
					|| leftWindow.distanceOnEdge(rightOverlap) < rightWindow.distanceOnEdge(rightOverlap))
			{
				leftList.add(new Window(leftWindow,leftWindow.b0,leftOverlap));
				rightList.add(new Window(rightWindow,leftOverlap,px));
				leftList.add(new Window(leftWindow,px,rightOverlap));
				
				if(leftWindow.b1 > rightWindow.b1){
					leftWindow.d0 = leftWindow.distanceOnEdge(rightOverlap);
					leftWindow.b0 = leftOverlap;
					leftList.add(leftWindow);
				}
				else
				{
					rightWindow.d0 = rightWindow.distanceOnEdge(rightOverlap);
					rightWindow.b0 = rightOverlap;
					rightList.add(rightWindow);
				}
				
			}
			else
			{
				if(leftWindow.b1 > rightWindow.b1){
					leftList.add(new Window(leftWindow,rightWindow.b1,leftWindow.b1));
				}
				
				leftWindow.d1 = leftWindow.distanceOnEdge(px);
				leftWindow.b1 = px ;
				leftList.add(leftWindow);
				
				rightWindow.d0 = rightWindow.distanceOnEdge(px);
				rightWindow.b0 = px;
				rightList.add(rightWindow);
			}
			
		}
		
		//We return the lists
		if(w1Left) {
			return new PairOfLists(leftList, rightList);
		} else {
			return new PairOfLists(rightList, leftList);
		}
	}
	
	public void merge(LinkedList<Window> windowsToMerge) throws Exception{
		
		//Before anything, we remove the empty windows
		LinkedList<Window> newWindowsToMerge = new LinkedList<Window>() ;
		for (Window newWindow : windowsToMerge){
			if(!ProjectUtils.equals(newWindow.b0, newWindow.b1))
			{
				newWindowsToMerge.push(newWindow);
			}
		}
		windowsToMerge = newWindowsToMerge;
		
		LinkedList<Window> existingWindows = this.computedWindows.get(windowsToMerge.getFirst().edge);
		
		if(existingWindows == null){
			existingWindows = new LinkedList<Window>() ;
			for( Window w : windowsToMerge ){
				existingWindows.add(w);
				this.windowsToPropagate.offer(w);
			}
			this.computedWindows.put(windowsToMerge.getFirst().edge, existingWindows);
			return ;
		}
		
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
					} else {
						//Or we can just keep the windows as is if they are compatible
						newResultingOldWindows.add(w);
						newWindowsToMerge.add(newWindow);
					}
				}
				resultingOldWindows = newResultingOldWindows;
			}
			windowsToMerge = newWindowsToMerge;
			
			newExistingWindows.addAll(resultingOldWindows);
			
			if(needsPropagation) {
				windowsToPropagate.addAll(resultingOldWindows);
			}
		}
		
		windowsToPropagate.addAll(windowsToMerge);
		newExistingWindows.addAll(windowsToMerge);
		existingWindows = newExistingWindows;
	}
	
	public void computeOnePropagation() throws Exception {
			Window w = this.windowsToPropagate.poll() ;
			LinkedList<Window> resOfPropagation = w.propagate() ;
			
			Halfedge<Point_3> e = resOfPropagation.peek().edge;
			
			LinkedList<Window> windowsOnFistEdge = new LinkedList<Window>(), lwindowsOnSecondEdge = new LinkedList<Window>();
			
			for(Window window : resOfPropagation)
			{
				if(window.edge == e)
				{
					windowsOnFistEdge.push(window);
				}
				else
				{
					lwindowsOnSecondEdge.push(window);
				}
			}
			
			if(!windowsOnFistEdge.isEmpty()){this.merge(windowsOnFistEdge);}
			if(!lwindowsOnSecondEdge.isEmpty()){this.merge(lwindowsOnSecondEdge);}
	}

	public void compute(int iteration) throws Exception {
		for(int i = 0 ; i < iteration ; i++){
			computeOnePropagation();
		}
	}
	
	public void compute() throws Exception {
		while(!this.windowsToPropagate.isEmpty()){
			computeOnePropagation();
		}
	}
}
