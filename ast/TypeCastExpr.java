package ast;

public class TypeCastExpr extends Expr {
    public final Type type;
    public final Expr expr;
    
    public TypeCastExpr (Type type , Expr expr) {
        this.type = type;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitTypeCastExpr(this);
    }
}
