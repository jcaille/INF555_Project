import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import Jcg.geometry.Point_2;


public class WindowTest {

	@Test
	public void testThirdVertex() {
		Random picker = new Random(12245);
		Window myWindow = new Window();
		for(int i=0 ; i<10000 ; i++){
			Point_2 expectedResult = new Point_2((picker.nextDouble()*10)-5, (picker.nextDouble()*10)-5);
			Point_2 secondVertex = new Point_2(picker.nextDouble()*5, 0);
			double b0 = 0 ;
			double b1 = secondVertex.x;
			double d0 = (Double) expectedResult.distanceFrom(new Point_2(0,0));
			double d1 = (Double) expectedResult.distanceFrom(secondVertex);
			int tau = (int) Math.signum(expectedResult.y) ;
			assertEquals("Y coordinates are not the same", expectedResult.y, myWindow.getThirdTriangleVertex(b0, b1, d0, d1, tau).y, 12);
			assertEquals("X Coordinates are not the same", expectedResult.x, myWindow.getThirdTriangleVertex(b0, b1, d0, d1, tau).x, 12);
			System.out.println(expectedResult.y +" "+ myWindow.getThirdTriangleVertex(b0, b1, d0, d1, tau).y);
		}		
	}

}
