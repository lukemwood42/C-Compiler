package sem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ast.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	Map<String, Type> envir;
	List<Type> returnT;
	Scope scope;
	boolean funDeclStart = false;
	@Override
	public Type visitBaseType(BaseType bt) {
		return bt;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl st) {
		StructTypeSymbol s = new StructTypeSymbol(st.structType.name, st.params);
		scope.put(s);
		return st.structType.accept(this);
	}

	@Override
	public Type visitBlock(Block b) {
		if (funDeclStart == false) {
			Scope oldScope = scope;
			scope = new Scope(oldScope);
			for (VarDecl vd : b.params) {
				visitVarDecl(vd);
			}
			for (Stmt s : b.stmts) {
				s.accept(this);
			}
			scope = oldScope;
		}
		else {
			funDeclStart = false;
			for (VarDecl vd : b.params) {
				vd.accept(this);
			}
			
			for (Stmt s : b.stmts) {
				s.accept(this);
			}
		}
		return null;
	}

	@Override
	public Type visitFunDecl(FunDecl p) {
		returnT = new ArrayList<>();
		funDeclStart = true;
		Scope oldScope = scope;
		scope = new Scope(oldScope);
		for (VarDecl vd : p.params) {
			vd.accept(this);
			//System.out.println(vd.type.getClass());
		}
		p.block.accept(this);
		for (Type t : returnT) {
			//if (t != p.type) //error("return and function type don't match");
		}
		returnT.clear();
		//System.out.println(p.name);
//		if (p.type != BaseType.VOID) {
//			if (returnT == null) {
//				//error("missing return statement");
//			}
//			else if (returnT != p.type) {
//				//error("return type doesn't match");
//			}
//		}
		return null;
	}


	@Override
	public Type visitProgram(Program p) {
		this.scope = new Scope();
		for (StructTypeDecl sd : p.structTypeDecls) {
			sd.accept(this);
		}
		for (VarDecl vd : p.varDecls) {
			vd.accept(this);
		}
		for (FunDecl fd : p.funDecls) {
			fd.accept(this);
		}
		return null;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) { 
		if (vd.type == BaseType.VOID) {
			//error("Type can't be void for VarDecl");
		}
		else {
			VarSymbol s = new VarSymbol(vd.varName, vd.type, 0);
			scope.put(s);
			return vd.type;
		}
		return null;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
		VarSymbol s = (VarSymbol) scope.lookup(v.name);
		if (s != null) {
		//	v.vd = s.getVs();
			return s.type;
		}
		return null;
	}

	@Override
	public Type visitPointerType(PointerType pointerType) {
		//System.out.println(pointerType.type.accept(this));
		return pointerType.type.accept(this);
	}

	@Override
	public Type visitReturn(Return return1) {
		if (return1.expr != null) {
			Type type = return1.expr.accept(this);
			returnT.add(type);
			return type;
		}
		return null;
	}

	@Override
	public Type visitOp(Op op) {
		return null;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr sizeOfExpr) {
		return BaseType.INT;
	}

	@Override
	public Type visitIf(If if1) {
		Type type = if1.expr.accept(this);
		if1.stmt1.accept(this);
		if (if1.stmt2 != null) {
			if1.stmt2.accept(this);
		}
		if (type != BaseType.INT) {
			//error("if expr should be type int");
		}
		return null;
	}

	@Override
	public Type visitIntLiteral(IntLiteral intLiteral) {
		intLiteral.type = BaseType.INT;
		return BaseType.INT;
	}

	@Override
	public Type visitStrLiteral(StrLiteral strLiteral) {
		int i = strLiteral.value.toCharArray().length;
		return (new ArrayType(BaseType.CHAR, i - 1)).accept(this);
	}

	@Override
	public Type visitExprStmt(ExprStmt exprStmt) {
		exprStmt.expr.accept(this);
		return null;
	}

	@Override
	public Type visitBinOp(BinOp binOp) {
		Type lhsT = binOp.lhs.accept(this);
		Type rhsT = binOp.rhs.accept(this);
		//System.out.println(lhsT + "     " + rhsT);
		if (binOp.op == Op.ADD || binOp.op == Op.SUB || binOp.op == Op.MUL || binOp.op == Op.DIV || binOp.op == Op.MOD || binOp.op == Op.OR || 
					binOp.op == Op.AND || binOp.op == Op.GT || binOp.op == Op.LT || binOp.op == Op.GE || binOp.op == Op.LE) {
			if (lhsT == BaseType.INT && rhsT == BaseType.INT) {
				binOp.type = BaseType.INT;
				return BaseType.INT;
			}
			else {
				//error("lhs and rhs type have to be int");
			}
			
		}
		else if (binOp.op == Op.NE || binOp.op == Op.EQ){
			if (lhsT != BaseType.VOID && !(lhsT instanceof StructType) && !(lhsT instanceof ArrayType)) {
				binOp.type = BaseType.INT;
				return BaseType.INT;
			}
			else {
				//error("lhsT has invalid type");
			}
		}
		else {
			//error("Op not valid");
		}
		return null;
	}

	@Override
	public Type visitAssign(Assign assign) {
		Expr expr = assign.lhs;
		Type lhsT = assign.lhs.accept(this);
		Type rhsT = assign.rhs.accept(this);
		if (lhsT instanceof PointerType || lhsT instanceof TypeCastExpr || lhsT instanceof ArrayType || lhsT instanceof FieldAccessExpr || lhsT instanceof ValueAtExpr) {
			lhsT = lhsT.accept(this);
		}
		if (rhsT instanceof PointerType || rhsT instanceof TypeCastExpr || rhsT instanceof ArrayType || rhsT instanceof FieldAccessExpr || rhsT instanceof ValueAtExpr) {
			rhsT = rhsT.accept(this);
		}
		//System.out.println(lhsT + " assign  " + rhsT);
		if (lhsT == BaseType.VOID || lhsT instanceof ArrayType) {
			//error("lhs for assign is wrong type");
		}
		if (!(expr instanceof VarExpr) && !(expr instanceof FieldAccessExpr) 
						&& !(expr instanceof ArrayAccessExpr)  && !(expr instanceof ValueAtExpr)) {
			//error("Lhs of assign is wrong type");
		}
		if (lhsT != rhsT) {
			//error("lhs and rhs types don't match");
		}
		return null;
	}

	@Override
	public Type visitChrLiteral(ChrLiteral chrLiteral) {
		chrLiteral.type = BaseType.CHAR;
		return BaseType.CHAR;
	}

	@Override
	public Type visitArrayType(ArrayType arrayType) {
		//System.out.println("240 " + arrayType.type);
		return arrayType.type.accept(this);
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
		Type array = arrayAccessExpr.array.accept(this);
		Type elemType = array.accept(this);
		Type index = arrayAccessExpr.index.accept(this);
		//System.out.println(array.getClass());
		if (array instanceof ArrayType || array instanceof PointerType) {
			if (index == BaseType.INT) {
				arrayAccessExpr.type = elemType;
				return elemType;
			}
			else {
				//error("index in array should be a int");
			}
		}
		else {
			//error("array access is not a array type or pointer type");
		}
		return null;
	}

	@Override
	public Type visitStructType(StructType structType) {
		
		return structType;
	}

	@Override
	public Type visitTypeCastExpr(TypeCastExpr typeCastExpr) {
		Type type = typeCastExpr.type;
		//System.out.println(type.getClass());
		//tem.out.println(typeCastExpr.type);
		//print_c fix is here!
		if (type instanceof ArrayType) {
			Type temp = type.accept(this);
			//System.out.println(temp == BaseType.CHAR);
			typeCastExpr.expr.type = new PointerType(temp);
			return temp;
		}
		else if (type instanceof PointerType) {
			Type temp = type.accept(this);
			type = new PointerType(temp);
			return temp;
		}
		else if (type == BaseType.CHAR) {
			typeCastExpr.expr.type = BaseType.INT;
			return BaseType.INT;
		}
		else {
			return type;
		}
		
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr valueAtExpr) {
		Type type = valueAtExpr.value.accept(this);
		if (type instanceof PointerType) {
			return type;
		}
		return null;
	}

	@Override
	public Type visitWhile(While while1) {
		while1.expr.accept(this);
		while1.stmt.accept(this);
		return null;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr fc) {
		if (fc.fd != null) {
			List<VarDecl> params = fc.fd.params;
			List<Expr> args = fc.exprs;
			if (params != null || args != null) {
				//System.out.println(fc.name + "    " + params.size() + "    " + args.size());
				if (params.size() == args.size()) {
					if (params.size() != 0) {
							for (int i = 0; i<params.size(); i++) {
								Type ta = args.get(i).accept(this);
								Type pt = params.get(i).type.accept(this);
								if (pt instanceof PointerType) {
									PointerType ptpt = (PointerType) pt;
									pt = ptpt.type;
								}
								if (ta instanceof ArrayType) {
									ArrayType tata = (ArrayType) ta;
									ta = tata.type;
								}
								if (ta instanceof PointerType) {
									PointerType tata = (PointerType) ta;
									ta = tata.type;
								}
								if (pt != ta && !(ta instanceof StructType || pt instanceof StructType)) {
									//System.out.println(pt + "       " + ta);
									//error("params and args don't have matching types");
								}
							}
						
					}
				}
				else {
					//error("params and args different sizes");
				}
			}
			fc.type = fc.fd.type;
			return fc.type;
		}
		return null;
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
		if (fieldAccessExpr.structure.accept(this) instanceof StructType) {
			if (fieldAccessExpr.structure instanceof VarExpr) {
				VarExpr v = (VarExpr) fieldAccessExpr.structure;
				StructType type = (StructType) v.accept(this);
				StructTypeSymbol s = (StructTypeSymbol) scope.lookup(type.name);
				for (VarDecl vd : s.getParams()) {
					if (vd.varName == fieldAccessExpr.name) {
						return vd.type;
					}
				}
			}
			else if (fieldAccessExpr.structure instanceof ArrayAccessExpr) {
				ArrayAccessExpr aae = (ArrayAccessExpr) fieldAccessExpr.structure;
				return aae.array.accept(this);
			}
					
			
		}
		else {
			//error("Field access needs struct type");
		}
		return null;
	}

	// To be completed...


}
