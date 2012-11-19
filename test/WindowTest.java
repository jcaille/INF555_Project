import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import Jcg.geometry.Point_2;


public class WindowTest {

	@Test
	public void testThirdVertex() {
		Random picker = new Random(12245);
		Window myWindow = new Window();
		for(int i=0 ; i<10 ; i++){
			Point_2 expectedResult = new Point_2((picker.nextDouble()*10)-5, (picker.nextDouble()*10)-5);
			Point_2 secondVertex = new Point_2(picker.nextDouble()*5, 0);
			double b0 = 0 ;
			double b1 = secondVertex.x;
			double d0 = (Double) expectedResult.distanceFrom(new Point_2(0,0));
			double d1 = (Double) expectedResult.distanceFrom(secondVertex);
			int tau = (int) Math.signum(expectedResult.y) ;
			assertEquals("Y coordinates are not the same", expectedResult.y, myWindow.getThirdTriangleVertex(b0, b1, d0, d1, tau).y, 12);
			assertEquals("X Coordinates are not the same", expectedResult.x, myWindow.getThirdTriangleVertex(b0, b1, d0, d1, tau).x, 12);
			//System.out.println(expectedResult.y +" "+ myWindow.getThirdTriangleVertex(b0, b1, d0, d1, tau).y);
		}
	}
	
	@Test
	public void testIntersectionOfLines() {
		Random picker = new Random(12345) ;
		Window myWindow = new Window() ;
		
		//test with two zero-length lines ;
		Point_2  a = new Point_2(0,0);
		assertNull(myWindow.intersectLines(a, a, a, a));
		
		//test with a zero-length line ;
		Point_2 b = new Point_2(1,1) ;
		Point_2 c = new Point_2(0,1) ;
		assertNull(myWindow.intersectLines(a, a, b, c));
		
		//test with two identical lines ;
		assertNull(myWindow.intersectLines(a, b, a, b));
		
		//test with two lines that do not intersect at all;
		Point_2 d = new Point_2(1,2) ;
		assertNull(myWindow.intersectLines(a, b, c, d));
		
		//test with two lines that do cross, but outside the segment
		Point_2 e = new Point_2(0,2) ;
		assertNull(myWindow.intersectLines(a, b, c, e)) ;
		
		//test with lines that intersect at one end of the segment
		assertEquals(a, myWindow.intersectLines(a, b, a, c)) ;
		
		//final test
		Point_2 f = new Point_2(2,2) ;
		Point_2 g = new Point_2(2,0) ;
		assertEquals(b, myWindow.intersectLines(a, f, e, g));
		
		//test reversability
		assertEquals(myWindow.intersectLines(a, f, e, g), myWindow.intersectLines(e, g, a, f));
		assertEquals(myWindow.intersectLines(a, f, e, g), myWindow.intersectLines(f, a, e, g)) ;

		//test if two lines are parallel but intersecting at only one point
		assertNull(myWindow.intersectLines(a, b, b, f));
	}
}

