package sem;

import java.util.List;

import ast.Block;
import ast.FunDecl;
import ast.Type;
import ast.VarDecl;

public class FunSymbol extends Symbol {

	private Type type;
	private List<VarDecl> params;
	private Block block;

	public FunSymbol(String name, Type type, List<VarDecl> params, Block block, int stackCount) {
		super(name, stackCount);
		this.type = type;
		this.params = params;
		this.block = block;
	}

	public FunDecl getFd() {
		return new FunDecl(this.type, super.name, this.params, this.block);
	}

}
