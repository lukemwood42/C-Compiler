package ast;

import java.io.PrintWriter;
import java.util.List;

public class ASTPrinter implements ASTVisitor<Void> {

    private PrintWriter writer;

    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    @Override
    public Void visitBlock(Block b) {
        writer.print("Block(");
        String temp = "";
        if (!b.params.isEmpty()) {
        	for (VarDecl vd : b.params) {
        		writer.print(temp);
        		vd.accept(this);
        		temp = ",";
        	}
        }      
        if (!b.stmts.isEmpty()) {
        	for (Stmt stmt : b.stmts) {
        		writer.print(temp);
        		stmt.accept(this);
        		temp = ",";
        	}
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl fd) {
        writer.print("FunDecl(");
        fd.type.accept(this);
        writer.print(","+fd.name+",");
        for (VarDecl vd : fd.params) {
            vd.accept(this);
            writer.print(",");
        }
        fd.block.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.print("Program(");
        String delimiter = "";
        for (StructTypeDecl std : p.structTypeDecls) {
            writer.print(delimiter);
            delimiter = ",";
            std.accept(this);
        }
        for (VarDecl vd : p.varDecls) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        for (FunDecl fd : p.funDecls) {
            writer.print(delimiter);
            delimiter = ",";
            fd.accept(this);
        }
        writer.print(")");
	    writer.flush();
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd){
        writer.print("VarDecl(");
        vd.type.accept(this);
        writer.print(","+vd.varName);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        writer.print("VarExpr(");
        writer.print(v.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        writer.print(bt);
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl st) {
        writer.print("StructTypeDecl(");
        st.structType.accept(this);
        for (VarDecl vd : st.params) {
        	writer.print(",");
        	vd.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIntLiteral(IntLiteral n) {
        writer.print("IntLiteral(");
        writer.print(n.value);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral s) {
        writer.print("StrLiteral(");
        writer.print(s.value);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral c) {
        writer.print("ChrLiteral(");
        writer.print(c.value);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunCallExpr(FunCallExpr f) {
        writer.print("FunCallExpr(");
        writer.print(f.name);
        for (Expr exp : f.exprs) {
        	writer.print(",");
        	exp.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        writer.print("BinOp(");
        bo.lhs.accept(this);
        writer.print(",");
        bo.op.accept(this);
        writer.print(",");
        //System.out.println(bo.rhs.getClass().toString());
        bo.rhs.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitOp(Op op) {
        writer.print(op);
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aa) {
        writer.print("ArrayAccessExpr(");
        aa.array.accept(this);
        writer.print(",");
        aa.index.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fa) {
        writer.print("FieldAccessExpr(");
        fa.structure.accept(this);
        writer.print(",");
        writer.print(fa.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr va) {
        writer.print("ValueAtExpr(");
        va.value.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr so) {
        writer.print("SizeOfExpr(");
        so.type.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitTypeCastExpr(TypeCastExpr tc) {
        writer.print("TypecastExpr(");
        tc.type.accept(this);
        writer.print(",");
        tc.expr.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt stmt) {
    	writer.print("ExprStmt(");
        stmt.expr.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitWhile(While w) {
        writer.print("While(");
        w.expr.accept(this);
        writer.print(",");
        w.stmt.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIf(If i) {
        writer.print("If(");
        i.expr.accept(this);
        writer.print(",");
        i.stmt1.accept(this);
        if (i.stmt2 != null) {
        	writer.print(",");
        	i.stmt2.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitAssign(Assign ass) {
        writer.print("Assign(");
        ass.lhs.accept(this);
        writer.print(",");
        ass.rhs.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitReturn(Return re) {
        writer.print("Return(");
        if (re.expr != null) {
        	re.expr.accept(this);
        }
        writer.print(")");
        return null;
    }

	@Override
	public Void visitPointerType(PointerType pointerType) {
		writer.print("PointerType(");
		pointerType.type.accept(this);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType arrayType) {
		writer.print("arraytype(");
		arrayType.type.accept(this);
		writer.print(",");
		writer.print(arrayType.numberOfElements);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitStructType(StructType structType) {
		writer.print("StructType(");
		writer.print(structType.name);
		writer.print(")");
		return null;
	}



    // to complete ...

}
