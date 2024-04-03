package sem;

import java.util.List;

import ast.VarDecl;

public class StructTypeSymbol extends Symbol {

	private List<VarDecl> params;
	public StructTypeSymbol(String name, List<VarDecl> params2) {
		super(name, 0);
		this.params = params2;
	}
	
	public List<VarDecl> getParams() {
		return params;
	}

}
