package uk.ac.gla.jagora;


public class ReceivedOrder<T extends Order> implements Comparable<ReceivedOrder<? extends Order>>, TickableEvent {

	public final T order;
	public final Long tick;
	
	public ReceivedOrder(T order, World world) {
		this.order = order;
		this.tick = world.getTick(this);
	}

	@Override
	public int compareTo(ReceivedOrder<? extends Order> receivedOrder) {
		Integer orderComparison =
			order.compareTo(receivedOrder.order);
		
		if (orderComparison == 0)
			return tick.compareTo(receivedOrder.tick);
		else return orderComparison;
	}	
	
	@Override
	public String toString (){
		return String.format("%s:t=%s", order, tick);
	}
}
