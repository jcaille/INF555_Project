import java.util.LinkedList;


public class PairOfLists {
	
	LinkedList<Window> first;
	LinkedList<Window> secund;

	public PairOfLists() {
		this.first = new LinkedList<Window>();
		this.secund = new LinkedList<Window>();
	}
	
	public PairOfLists(LinkedList<Window> first, LinkedList<Window> secund) {
		this.first = new LinkedList<Window>();
		this.secund = new LinkedList<Window>();
		
		for(Window w:first)
		{
			if(!w.isEmpty())
			{
				this.first.push(w);
			}
		}
		
		for(Window w:secund)
		{
			if(!w.isEmpty())
			{
				this.secund.push(w);
			}
		}
	}
}
