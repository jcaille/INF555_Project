import java.util.Comparator;


public class WindowComparator2 implements Comparator<Window> {

	public WindowComparator2() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Window arg0, Window arg1) {
		double score0 = arg0.b1 ;
		double score1 = arg1.b1 ;
		if(score0  > score1){
			return 1 ;
		} 
		if(score0 < score1){
			return -1 ;
		}
		return 0;
	}
	

}
