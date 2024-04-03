package sem;

import ast.Type;
import ast.VarDecl;

public class VarSymbol extends Symbol {

	Type type;
	public VarSymbol(String varName, Type type, int stackCount) {
		super(varName, stackCount);
		this.type = type;
	}
	public VarDecl getVs() {
		return new VarDecl(this.type, super.name, super.stackCount);
	}

}
