package sem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ast.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {


	Scope scope;
//	Map<String, Symbol> oldTable = null;
	int stackCount = 0;
	boolean funDeclStart = false;
	boolean globalVars = true;
	
	@Override
	public Void visitBaseType(BaseType bt) {
		return null;
	}

	@Override
	public Void visitStructTypeDecl(StructTypeDecl sts) {
		String name = sts.structType.name;
		Symbol s = scope.lookup(name);
		if (s != null) {
			super.error("StructType already declared");
		}
		else
			s = new StructTypeSymbol(name, sts.params);
			scope.put(s);
		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		if (funDeclStart == false) {
			Scope oldScope = scope;
			scope = new Scope(oldScope);
			if (!b.params.isEmpty()) {
				for (VarDecl vd : b.params) {
					visitVarDecl(vd);
				}
			}
			if (!b.stmts.isEmpty()) {
				for (Stmt s : b.stmts) {
					visitStmt(s);
				}
			}
			scope = oldScope;
		}
		else {
			funDeclStart = false;
			if (!b.params.isEmpty()) {
				for (VarDecl vd : b.params) {
					visitVarDecl(vd);
					//System.out.println(vd.stackCount);
				}
			}
			if (!b.stmts.isEmpty()) {
				for (Stmt s : b.stmts) {
					visitStmt(s);
				}
			}
		}
		
		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl p) {
		//System.out.println(p.name);
		Symbol s = scope.lookup(p.name);
		if (s != null) {
			super.error("Function already declared");
		}
		else
			s = new FunSymbol(p.name, p.type, p.params, p.block, stackCount);
			scope.put(s);
			int tempStackCount = stackCount;
			funDeclStart = true;
//			oldTable = new HashMap<>(scope.getSymbolTable());
			//System.out.println("oldtable size - " + oldTable.size());
			//System.out.println(p.params.size());
			Scope oldScope = scope;
			scope = new Scope(oldScope);
			if (p.params != null) {
				for (VarDecl vd : p.params) {
					visitVarDecl(vd);
					//System.out.println(vd.varName + "  " + vd.stackCount);
				}
				stackCount += p.params.size() * 4;
			}	
			if (p.block != null) {
				visitBlock(p.block);
			}
//			scope.setTable(oldTable);
			stackCount = tempStackCount;
			scope = new Scope(oldScope);
			funDeclStart = false;
		return null;
	}


	@Override
	public Void visitProgram(Program p) {
		this.scope = new Scope();
//		this.oldTable = new HashMap<>();
		for (StructTypeDecl sd : p.structTypeDecls) {
			visitStructTypeDecl(sd);
		}
		for (VarDecl vd : p.varDecls) {
			visitVarDecl(vd);
		}
		globalVars = false;
		visitBuiltInFuns();
		stackCount = 0;
		for (FunDecl fd : p.funDecls) {
			visitFunDecl(fd);
		}
		return null;
	}
	
	public Void visitBuiltInFuns() {
		Symbol s;
		Block block = new Block(new ArrayList<>(), new ArrayList<>());
		List<VarDecl> vds = new ArrayList<>();
		//Type tc = (Type) new TypeCastExpr(new PointerType(BaseType.CHAR), new StrLiteral("tc")).type;
		vds.add(new VarDecl(new PointerType(BaseType.CHAR), "s", stackCount));
		s = new FunSymbol("print_s", BaseType.VOID, vds, block, stackCount);
		scope.put(s);
		//visitFunDecl(new FunDecl(BaseType.VOID, "print_s", vds, block));
		vds = new ArrayList<>();
		vds.add(new VarDecl(BaseType.INT, "i", stackCount));
		s = new FunSymbol("print_i", BaseType.VOID, vds, block, stackCount);
		scope.put(s); 
		vds = new ArrayList<>();
		vds.add(new VarDecl(BaseType.CHAR, "s", stackCount));
		s = new FunSymbol("print_c", BaseType.VOID, vds, block, stackCount);
		scope.put(s);
		vds = new ArrayList<>();
		s = new FunSymbol("read_c", BaseType.CHAR, new ArrayList<>(), block, stackCount);
		scope.put(s);
		s = new FunSymbol("read_i", BaseType.INT, new ArrayList<>(), block, stackCount);
		scope.put(s);
		vds.add(new VarDecl(BaseType.INT, "size", stackCount));
		s = new FunSymbol("mcmalloc", new PointerType(BaseType.VOID), vds, block, stackCount);
		scope.put(s);
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		//System.out.println(vd.varName);
		Symbol s = scope.lookupCurrent(vd.varName);
		if (s != null) {
//			super.error("Variable already declared");
		}
		else if (globalVars == true){
			s = new VarSymbol(vd.varName, vd.type, -1);
			vd.stackCount = -1;
			scope.put(s);
		}
		else {
			s = new VarSymbol(vd.varName, vd.type, stackCount);
			vd.stackCount = stackCount;
			scope.put(s);
			stackCount += 4;
		}
			//System.out.println(vd.type);
			//System.out.println(scope.symbolTable.size());
		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
		//System.out.println(v.name);
		Symbol s = scope.lookup(v.name);
		if (s instanceof VarSymbol || s == null) {
			if (s == null) {
				super.error("Variable not declared");
			}
			else {
				VarSymbol vs = (VarSymbol) s;
				v.vd = vs.getVs();
				//System.out.println(v.vd.stackCount);
			}
		}
		else {
//			super.error("lhs should be varExpr");
		}
		
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr fc) {
		//System.out.println(fc.name);
		
		Symbol s = scope.lookup(fc.name);
		if (s instanceof FunSymbol) {
			if (s == null) {
				super.error("Function not declared");
			}
			for (Expr vd : fc.exprs) {
				visitExpr(vd);
			}
			if (s != null) {
				FunSymbol fs = (FunSymbol) s;
				fc.fd = fs.getFd();
			}
		}
		else {
			super.error("function not declared");
		}
		
		return null;
	}

	public Void visitStmt(Stmt s) {
		if (s instanceof Block) {
			Block b = (Block) s;
			visitBlock(b);
		}
		else if (s instanceof ExprStmt) {
			ExprStmt es = (ExprStmt) s;
			visitExprStmt(es);
		}
		else if (s instanceof While) {
			While w = (While) s;
			visitWhile(w);
		}
		else if (s instanceof If) {
			If i = (If) s;
			visitIf(i);
		}
		else if (s instanceof Assign) {
			Assign ass = (Assign) s;
			visitAssign(ass);
		}
		else if (s instanceof Return){
			Return r = (Return) s;
			visitReturn(r);
		}
		return null;
	}

	private Void visitExpr(Expr expr) {
		if (expr instanceof VarExpr) {
			VarExpr v = (VarExpr) expr;
			visitVarExpr(v);
		}
		else if (expr instanceof FunCallExpr) {
			FunCallExpr fc = (FunCallExpr) expr;
			visitFunCallExpr(fc);
		}
		else if (expr instanceof BinOp) {
			BinOp bo = (BinOp) expr;
			visitBinOp(bo);
		}
		else if (expr instanceof ArrayAccessExpr) {
			ArrayAccessExpr aa = (ArrayAccessExpr) expr;
			visitArrayAccessExpr(aa);
		}
		else if (expr instanceof FieldAccessExpr){
			FieldAccessExpr fa = (FieldAccessExpr) expr;
			visitFieldAccessExpr(fa);
		}
		else if (expr instanceof ValueAtExpr){
			ValueAtExpr va = (ValueAtExpr) expr;
			visitValueAtExpr(va);
		}
		else if (expr instanceof TypeCastExpr){
			TypeCastExpr tc = (TypeCastExpr) expr;
			visitTypeCastExpr(tc);
		}
		return null;
	}

	@Override
	public Void visitExprStmt(ExprStmt ExprStmt) {
		visitExpr(ExprStmt.expr);
		return null;
	}



	@Override
	public Void visitWhile(While w) {
		visitExpr(w.expr);
		visitStmt(w.stmt);
		return null;
	}

	@Override
	public Void visitIf(If i) {
		visitExpr(i.expr);
		visitStmt(i.stmt1);
		if (i.stmt2 != null) {
			visitStmt(i.stmt2);
		}
		return null;
	}

	@Override
	public Void visitAssign(Assign ass) {
		visitExpr(ass.lhs);
		visitExpr(ass.rhs);
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		if (r.expr != null) {
			visitExpr(r.expr);
		}
		return null;
	}

	@Override
	public Void visitPointerType(PointerType pointerType) {
		return null;
	}

	@Override
	public Void visitOp(Op op) {
		return null;
	}

	@Override
	public Void visitSizeOfExpr(SizeOfExpr sizeOfExpr) {
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral intLiteral) {
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral strLiteral) {
		return null;
	}

	@Override
	public Void visitBinOp(BinOp binOp) {
		visitExpr(binOp.lhs);
		visitExpr(binOp.rhs);
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral chrLiteral) {
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType arrayType) {
		
		return null;
	}

	@Override
	public Void visitArrayAccessExpr(ArrayAccessExpr aa) {
		Expr expr = aa.array;
		if (expr instanceof VarExpr) {
			VarExpr v = (VarExpr) expr;
			Symbol s = scope.lookup(v.name);
			if (s instanceof VarSymbol || s == null) {
				if (s == null) {
					super.error("Variable not declared");
				}
				else {
					VarSymbol vs = (VarSymbol) s;
					aa.vd = vs.getVs();
				}
			}
			else {
				super.error("lhs should be varExpr");
			}
			if (aa.index instanceof IntLiteral) {
				IntLiteral n = (IntLiteral) aa.index.type;
			}
			else if (aa.index instanceof VarExpr){
				aa.index.accept(this);
			}
			
		}
		return null;
	}

	@Override
	public Void visitStructType(StructType st) {
		String name = st.name;
		Symbol s = scope.lookup(name);
		if (s instanceof StructTypeSymbol) {
			if (s == null) {
				super.error("StructType not declared");
			}
		}
		else {
			super.error("st should be structType");
		}	
		return null;
	}

	@Override
	public Void visitTypeCastExpr(TypeCastExpr tc) {
		visitExpr(tc.expr);
		return null;
	}

	@Override
	public Void visitValueAtExpr(ValueAtExpr va) {
		visitExpr(va.value);
		return null;
	}

	@Override
	public Void visitFieldAccessExpr(FieldAccessExpr fa) {
		visitExpr(fa.structure);
		return null;
	}


	// To be completed...


}
