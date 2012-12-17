import java.util.LinkedList;


public class PairOfLists {
	
	LinkedList<Window> first;
	LinkedList<Window> second;

	public PairOfLists() {
		this.first = new LinkedList<Window>();
		this.second = new LinkedList<Window>();
	}
	
	public PairOfLists(LinkedList<Window> first, LinkedList<Window> secund) {
		this.first = new LinkedList<Window>();
		this.second = new LinkedList<Window>();
		
		for(Window w:first)
		{
			if(!w.isEmpty())
			{
				this.first.push(w);
			}
		}
		
		for(Window w:second)
		{
			if(!w.isEmpty())
			{
				this.second.push(w);
			}
		}
	}
}
