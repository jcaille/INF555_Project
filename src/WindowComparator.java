import java.util.Comparator;


public class WindowComparator implements Comparator<Window> {

	public WindowComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Window arg0, Window arg1) {
		double score0 = arg0.getMinDistanceToSource() ;
		double score1 = arg1.getMinDistanceToSource() ;
		if(score0  < score1){
			return 1 ;
		} 
		if(score1 < score0){
			return -1 ;
		}
		return 0;
	}
	

}
