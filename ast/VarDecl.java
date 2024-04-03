package ast;

public class VarDecl implements ASTNode {
    public final Type type;
    public final String varName;
    public int stackCount;

    public VarDecl(Type type, String varName, int stackCount) {
	    this.type = type;
	    this.varName = varName;
	    this.stackCount = stackCount;
    }

     public <T> T accept(ASTVisitor<T> v) {
	return v.visitVarDecl(this);
    }
}
