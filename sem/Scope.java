package sem;

import java.util.HashMap;
import java.util.Map;

public class Scope {
	private Scope outer;
	private Map<String, Symbol> symbolTable;
	
	public Scope(Scope outer) { 
		this.outer = outer; 
		this.symbolTable = new HashMap<>();
	}
	
	public Scope() { 
		this.outer = null;
		this.symbolTable = new HashMap<>();
	}
	
	public Symbol lookup(String name) {
		if (lookupCurrent(name) != null) {
			return lookupCurrent(name);
		}
		Scope temp = outer;
		while (temp != null) {
			if (temp.lookupCurrent(name) != null) {
				return temp.lookupCurrent(name);
			}
			temp = temp.outer;
		}
		return null;
	}
	
	public Symbol lookupCurrent(String name) {
		return symbolTable.get(name);
	}
	
	public void put(Symbol sym) {
		symbolTable.put(sym.name, sym);
	}
	
}
