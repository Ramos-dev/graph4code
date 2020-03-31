package utils;

public class Pair<X, Y> {
	
	private final X x;
	private final Y y;
	
	private Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	public static <X,Y>  Pair<X,Y> create(X x, Y y)
	{
		return new Pair<>(x,y);
	}


	public X getFirst() {
		return x;
	}


	public Y getSecond() {
		return y;
	}
	
	

	
}
