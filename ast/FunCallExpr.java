package ast;

import java.util.List;

public class FunCallExpr extends Expr {
    public final String name;
    public final List<Expr> exprs;
    public FunDecl fd;

    public FunCallExpr(String name, List<Expr> exprs){
	   this.name = name;
       this.exprs = exprs;
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitFunCallExpr(this);
    }
}
