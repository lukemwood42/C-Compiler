package gen;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import ast.*;

public class StringTypes{

	Writer writer;
	Program p;
	ArrayList<String> strs = new ArrayList<>();
	ArrayList<Integer> mallocs = new ArrayList<>();
	HashMap<String, ArrayList<String>> structs = new HashMap<>();
	
	public StringTypes(PrintWriter writer, Program p) {
		this.writer = writer;
		this.p = p;
	}

	public Register visitBaseType(BaseType bt) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitStructTypeDecl(StructTypeDecl std) {
		StructType st = std.structType;
		String name = st.name;
		int n = 0;
		ArrayList<String> vds = new ArrayList<>();
		for (VarDecl vd : std.params) {
			if (vd.type instanceof StructType) {
				StructType temp = (StructType) vd.type;
				vds.addAll(structs.get(temp.name));
			}
			else vds.add(vd.varName);
		}
		structs.put(name, vds);
		return null;
	}

	public Register visitBlock(Block b) {
		for (VarDecl vd : b.params) {
			if (vd.type instanceof ArrayType) {
				ArrayType at = (ArrayType) vd.type;
				int n = at.numberOfElements;
//				writer.println(vd.varName + ":   .space   " + (n * 4));
			}
		}
		for (Stmt st : b.stmts) {
    		if (st instanceof ExprStmt) {
    			ExprStmt es = (ExprStmt) st;
    			if (es.expr instanceof FunCallExpr) {
    				visitFunCallExpr((FunCallExpr) es.expr);
    			}
        		else if (es.expr instanceof TypeCastExpr) {
        			visitTypeCastExpr((TypeCastExpr) es.expr);
        		}
    		}
    		else if (st instanceof If) {
    			If if1 = (If) st;
    			visitIf(if1);
    		}
    		else if (st instanceof While) {
    			While while1 = (While) st;
    			visitWhile(while1);
    		}
    		else if (st instanceof Assign) {
    			visitAssign((Assign) st);
    		}
    		
    	}
		return null;
	}

	public Register visitFunDecl(FunDecl p) {
		visitBlock(p.block);
		return null;
	}

	public ArrayList<String> visitProgram(Program p) {
		for (StructTypeDecl std : p.structTypeDecls) visitStructTypeDecl(std);
		for (FunDecl fd : p.funDecls) {
			visitFunDecl(fd);
		}
		return strs;
	}

	public Register visitVarDecl(VarDecl vd) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitVarExpr(VarExpr v) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitPointerType(PointerType pointerType) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitReturn(Return return1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitOp(Op op) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitSizeOfExpr(SizeOfExpr sizeOfExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitIf(If if1) {
		if (if1.stmt1 instanceof Block) {visitBlock((Block) if1.stmt1);}
		else if(if1.stmt1 instanceof ExprStmt) {
			ExprStmt es = (ExprStmt) if1.stmt1;
			if (es.expr instanceof FunCallExpr) {
				visitFunCallExpr((FunCallExpr) es.expr);
			}
		}
		else if(if1.stmt1 instanceof If) {
			If if2 = (If) if1.stmt1;
			visitIf(if2);
		}
		else if(if1.stmt1 instanceof While) {
			While while1 = (While) if1.stmt1;
			visitWhile(while1);
		}
		
		if (if1.stmt2 != null) {
		//	System.out.println("101");
			if (if1.stmt2 instanceof Block) {visitBlock((Block) if1.stmt2);}
			else if(if1.stmt2 instanceof ExprStmt) {
				ExprStmt es = (ExprStmt) if1.stmt2;
				if (es.expr instanceof FunCallExpr) {
					visitFunCallExpr((FunCallExpr) es.expr);
				}
			}
			else if(if1.stmt2 instanceof If) {
				If if2 = (If) if1.stmt2;
				visitIf(if2);
			}
			else if(if1.stmt2 instanceof While) {
				While while1 = (While) if1.stmt2;
				visitWhile(while1);
			}
		}
		return null;
	}

	public Register visitIntLiteral(IntLiteral intLiteral) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitStrLiteral(StrLiteral strLiteral) {
		strs.add(strLiteral.value);
		return null;
	}

	public Register visitExprStmt(ExprStmt exprStmt) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitBinOp(BinOp binOp) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitAssign(Assign assign) {
		if (assign.rhs instanceof TypeCastExpr) visitTypeCastExpr((TypeCastExpr) assign.rhs);
		else if (assign.rhs instanceof FunCallExpr) visitFunCallExpr((FunCallExpr) assign.rhs);
		return null;
	}

	public Register visitChrLiteral(ChrLiteral chrLiteral) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitArrayType(ArrayType arrayType) {
		return null;
	}

	public Register visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitStructType(StructType structType) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitTypeCastExpr(TypeCastExpr typeCastExpr) {
		if (typeCastExpr.expr instanceof FunCallExpr) {
			visitFunCallExpr((FunCallExpr) typeCastExpr.expr);
		}
		if (typeCastExpr.expr instanceof StrLiteral) {
			visitStrLiteral((StrLiteral) typeCastExpr.expr);
		}
		return null;
	}

	public Register visitValueAtExpr(ValueAtExpr valueAtExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitWhile(While while1) {
		if (while1.stmt instanceof Block) {visitBlock((Block) while1.stmt);}
		else if(while1.stmt instanceof ExprStmt) {
			ExprStmt es = (ExprStmt) while1.stmt;
			if (es.expr instanceof FunCallExpr) {
				visitFunCallExpr((FunCallExpr) es.expr);
			}
		}
		return null;
	}

	public Register visitFunCallExpr(FunCallExpr funCallExpr) {
		if (funCallExpr.name.equals("print_s")) {
			if (funCallExpr.exprs.get(0) instanceof TypeCastExpr) {
				TypeCastExpr tce = (TypeCastExpr) funCallExpr.exprs.get(0);
				if (tce.expr instanceof StrLiteral) {
					StrLiteral str = (StrLiteral) tce.expr;
					//System.out.println(str.value);
					visitStrLiteral(str);
				}
			}
		}
		else if (funCallExpr.name.equals("mcmalloc")) {
			if (funCallExpr.exprs.get(0) instanceof TypeCastExpr) {
				TypeCastExpr tce = (TypeCastExpr) funCallExpr.exprs.get(0);
				IntLiteral n = (IntLiteral) tce.expr;
				mallocs.add(n.value);
			}
			else if (funCallExpr.exprs.get(0) instanceof SizeOfExpr) {
				SizeOfExpr soe = (SizeOfExpr) funCallExpr.exprs.get(0);
				if (soe.type instanceof StructType) {
					StructType st = (StructType) soe.type;
					mallocs.add((structs.get(st.name).size()) * 4);
				}
				else mallocs.add(4);

			}
			
		}
		return null;
	}

	public Register visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Integer> getMallocs() {
		return this.mallocs;
	}
}
