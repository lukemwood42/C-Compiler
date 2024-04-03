package ast;

public interface ASTVisitor<T> {
    public T visitBaseType(BaseType bt);
    public T visitStructTypeDecl(StructTypeDecl st);
    public T visitBlock(Block b);
    public T visitFunDecl(FunDecl p);
    public T visitProgram(Program p);
    public T visitVarDecl(VarDecl vd);
    public T visitVarExpr(VarExpr v);
	public T visitPointerType(PointerType pointerType);
	public T visitReturn(Return return1);
	public T visitOp(Op op);
	public T visitSizeOfExpr(SizeOfExpr sizeOfExpr);
	public T visitIf(If if1);
	public T visitIntLiteral(IntLiteral intLiteral);
	public T visitStrLiteral(StrLiteral strLiteral);
	public T visitExprStmt(ExprStmt exprStmt);
	public T visitBinOp(BinOp binOp);
	public T visitAssign(Assign assign);
	public T visitChrLiteral(ChrLiteral chrLiteral);
	public T visitArrayType(ArrayType arrayType);
	public T visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr);
	public T visitStructType(StructType structType);
	public T visitTypeCastExpr(TypeCastExpr typeCastExpr);
	public T visitValueAtExpr(ValueAtExpr valueAtExpr);
	public T visitWhile(While while1);
	public T visitFunCallExpr(FunCallExpr funCallExpr);
	public T visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr);

    // to complete ... (should have one visit method for each concrete AST node class)
}
