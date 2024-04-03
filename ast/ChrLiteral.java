package ast;

public class ChrLiteral extends Expr {
    public final String value;

    public ChrLiteral(String value){
	     this.value = value;
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitChrLiteral(this);
    }
}
