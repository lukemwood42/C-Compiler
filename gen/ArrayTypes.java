package gen;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import ast.*;

public class ArrayTypes{

	Writer writer;
	Program p;
	HashMap<String, Integer> arrays = new HashMap<>();
	HashMap<String, ArrayList<String>> structs = new HashMap<>();
	
	public ArrayTypes(PrintWriter writer, Program p) {
		this.writer = writer;
		this.p = p;
	}

	public Register visitBaseType(BaseType bt) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitStructTypeDecl(StructTypeDecl st) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitBlock(Block b) {
		for (VarDecl vd : b.params) {
			visitVarDecl(vd);
		}
		for (Stmt st : b.stmts) {
			if (st instanceof Block) {
				visitBlock((Block) st);
			}
		}
		return null;
	}

	public Register visitFunDecl(FunDecl p) {
		for (VarDecl vd : p.params) {
			visitVarDecl(vd);
		}
		visitBlock(p.block);
		return null;
	}

	public HashMap<String, Integer> visitProgram(Program p) {
		for (StructTypeDecl std : p.structTypeDecls) {
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
		}
		for (VarDecl vd : p.varDecls) {
			visitVarDecl(vd);
		}
		for (FunDecl fd : p.funDecls) {
			visitFunDecl(fd);
		}
		return arrays;
	}

	public Register visitVarDecl(VarDecl vd) {
		if (vd.type instanceof ArrayType) {
			ArrayType at = (ArrayType) vd.type;
			arrays.put(vd.varName, (at.numberOfElements * 4));
		}
		if (vd.type instanceof PointerType) {
			PointerType pt = (PointerType) vd.type;
			if (pt.type instanceof StructType) {
				StructType type = (StructType) pt.type;
				structs.put(vd.varName, structs.get(type.name));
			}
		}
		if (vd.type instanceof StructType) {
			StructType type = (StructType) vd.type;
			structs.put(vd.varName, structs.get(type.name));
		}
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
		return null;
	}

	public Register visitIntLiteral(IntLiteral intLiteral) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitStrLiteral(StrLiteral strLiteral) {
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitValueAtExpr(ValueAtExpr valueAtExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	public Register visitWhile(While while1) {
		return null;
	}

	public Register visitFunCallExpr(FunCallExpr funCallExpr) {
		return null;
	}

	public Register visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<String, ArrayList<String>> getStructs() {
		return this.structs;
	}
}
