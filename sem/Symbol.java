package sem;

public abstract class Symbol {
	public String name;
	public int stackCount;
	
	public Symbol(String name, int stackCount) {
		this.name = name;
		this.stackCount = stackCount;
	}
}
