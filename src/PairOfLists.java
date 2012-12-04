import java.util.LinkedList;


public class PairOfLists {
	
	LinkedList<Window> first;
	LinkedList<Window> secund;

	public PairOfLists() {
		this.first = new LinkedList<Window>();
		this.secund = new LinkedList<Window>();
	}
	
	public PairOfLists(LinkedList<Window> first, LinkedList<Window> secund) {
		this.first = first;
		this.secund = secund;
	}

}
