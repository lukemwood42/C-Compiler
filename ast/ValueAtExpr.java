package ast;

public class ValueAtExpr extends Expr {
    public final Expr value;

    public ValueAtExpr (Expr value) {
        this.value = value;
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitValueAtExpr(this);
    }
}
