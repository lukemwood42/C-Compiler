package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

public class CodeGenerator implements ASTVisitor<Register> {

    /*
     * Simple register allocator.
     */

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    public CodeGenerator() {
        freeRegs.addAll(Register.tmpRegs);
    }

    private class RegisterAllocationError extends Error {}

    private Register getRegister() {
        try {
            return freeRegs.pop();
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck!
        }
    }

    private void freeRegister(Register reg) {
        freeRegs.push(reg);
    }

    
    
    private PrintWriter writer; // use this writer to output the assembly instructions


    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        writer = new PrintWriter(outputFile);

        visitProgram(program);
        writer.close();
    }
    
    int loopCount = -1;
    int ifCount = -1;
    int orAndCount = 0;
    int strCount = 0;
    int mallocCount = 0;
    HashMap<String, ArrayList<String>> structs;

    @Override
    public Register visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Register visitStructTypeDecl(StructTypeDecl st) {
        return null;
    }

    @Override
    public Register visitBlock(Block b) {
    	for (VarDecl vd : b.params) {
    		vd.accept(this);
    	}
    	for (Stmt st : b.stmts) {
    		st.accept(this);
    	}
        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl p) {
    	for (VarDecl vd : p.params) {
    		Register temp = getRegister();
        	writer.println("lw " + temp + ", -" + vd.stackCount + "($fp)");
            writer.println("sw " + temp + ", 0($sp)");
            freeRegister(temp);
        	writer.println("addi $sp, $sp, -4");
    	}
    	p.block.accept(this);
    	writer.println("bne $zero, $ra, return");
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitProgram(Program p) {
    	writer.println(".data");
    	StringTypes st = new StringTypes(writer, p);
    	ArrayList<String> strs = st.visitProgram(p);
    	
    	for (int i : st.getMallocs()) { //making space for malloc
    		writer.println("malloc" + mallocCount + ":   .space " + i);
    		mallocCount++;
    	}
    	mallocCount = 0;
    	
    	ArrayTypes at = new ArrayTypes(writer, p);
    	HashMap<String, Integer> arrays = at.visitProgram(p);
    	for (String vdName : arrays.keySet()) { //Arrays 
    		int n = arrays.get(vdName);
    		writer.println(vdName + ":   .space " + n);
    	}
    	
    	HashMap<String, ArrayList<String>> structs = at.getStructs();
    	this.structs = structs;
    	for (String s : structs.keySet()) {
    		writer.println("Struct_" + s + ":   .space " + (structs.get(s).size() * 4));
        }
    	
    	for (String s : strs) { //for print_s
    		writer.println("str" + strCount + ":   .asciiz \"" + s + "\"");
    		strCount++;
    	}
    	strCount = 0;
    	
        for (VarDecl vd : p.varDecls) { //Global Variables not including arrays
        	if (!(vd.type instanceof ArrayType || vd.type instanceof StructType)) {
        		writer.println(vd.varName + ":      .word 0");
        	}
        }
     	writer.println(".text");
    	writer.println("add $fp, $sp, $zero");
    	writer.println("j main");
        for (FunDecl fd : p.funDecls) {
        	writer.println(fd.name + ":   ");
            fd.accept(this);
        }
     	writer.println("li $v0, 10");
		writer.println("syscall");
        writer.println("return: jr $ra");
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
    	if (!(vd.type instanceof ArrayType || vd.type instanceof StructType)) {
    		writer.println("addi $sp, $sp, -4");
    	}
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
    	Register result = getRegister();
    	if (v.vd.type instanceof StructType) {
        	return v.vd.type.accept(this);
        }

    	if (v.vd.stackCount == -1) {
    		writer.println("lw " + result + ", " + v.vd.varName);
    	}
    	else {
    		writer.println("lw " + result + ", -" + v.vd.stackCount + "($fp)");
    	}
        return result;
    }

	@Override
	public Register visitPointerType(PointerType pointerType) {
		return pointerType.type.accept(this);
	}

	@Override
	public Register visitReturn(Return return1) {
		if (return1.expr != null) {
			Register temp = return1.expr.accept(this);
			writer.println("add $v0, $zero, " + temp);
			freeRegister(temp);
		}
		writer.println("bne $zero, $ra, return");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Register visitOp(Op op) {
		return null;
	}

	@Override
	public Register visitSizeOfExpr(SizeOfExpr sizeOfExpr) {
		Register result = getRegister();
		Type type = sizeOfExpr.type;
		if (type == BaseType.INT) writer.println("li " + result + ", 4");
		if (type == BaseType.CHAR) writer.println("li " + result + ", 4");
		else result = type.accept(this);
		return result;
	}

	@Override
	public Register visitIf(If if1) {
		ifCount++;
		int ifCountTemp = ifCount;
		Register exprReg = if1.expr.accept(this);
		if (if1.stmt2 != null) {
			writer.println("beq $zero, " + exprReg + " else" + ifCountTemp);
			freeRegister(exprReg);
			if1.stmt1.accept(this);
			writer.println("j end" + ifCountTemp);
			writer.println("else" + ifCountTemp + ":   ");
			if1.stmt2.accept(this);
		}
		else {
			writer.println("beq $zero, " + exprReg + " end" + ifCountTemp);
			freeRegister(exprReg);
			if1.stmt1.accept(this);
		}
		writer.println("end" + ifCountTemp + ":   ");
		return null;
	}

	@Override
	public Register visitIntLiteral(IntLiteral intLiteral) {
		Register result = getRegister();
		writer.println("li " + result + ", " + intLiteral.value);
		return result;
	}

	@Override
	public Register visitStrLiteral(StrLiteral strLiteral) {
		Register result = getRegister();
		writer.println("la " + result + ", str" + strCount);
		return result;
	}

	@Override
	public Register visitExprStmt(ExprStmt exprStmt) {
		return exprStmt.expr.accept(this);
	}

	@Override
	public Register visitBinOp(BinOp binOp) {
		if (binOp.op == Op.OR || binOp.op == Op.AND) {
			return visitBinOp2(binOp);
		}
		else {
			Register lhsReg = binOp.lhs.accept(this);
			Register rhsReg = binOp.rhs.accept(this);
			if (binOp.lhs instanceof ArrayAccessExpr) writer.println("lw " + lhsReg + ", 0(" + lhsReg + ")");
			if (binOp.rhs instanceof ArrayAccessExpr) writer.println("lw " + rhsReg + ", 0(" + rhsReg + ")");
			if (binOp.lhs instanceof FieldAccessExpr) writer.println("lw " + lhsReg + ", 0(" + lhsReg + ")");
			if (binOp.rhs instanceof FieldAccessExpr) writer.println("lw " + rhsReg + ", 0(" + rhsReg + ")");
			if (binOp.lhs instanceof ValueAtExpr) writer.println("lw " + lhsReg + ", 0(" + lhsReg + ")");
			if (binOp.rhs instanceof ValueAtExpr) writer.println("lw " + rhsReg + ", 0(" + rhsReg + ")");
			Register result = getRegister();
			switch (binOp.op) {
			case ADD: 
				writer.println("add " + result + ", " + lhsReg + ", " + rhsReg);
				break;
			case SUB: 
				writer.println("sub " + result + ", " + lhsReg + ", " + rhsReg);
				break;
			case MUL: 
				writer.println("mul " + result + ", " + lhsReg + ", " + rhsReg);
				break;
			case DIV: 
				writer.println("div " + result + ", " + lhsReg + ", " + rhsReg);
				break;
			case MOD: 
				writer.println("div " + result + ", " + lhsReg + ", " + rhsReg);
				Register mflo = getRegister();
				writer.println("mfhi " + mflo);
				writer.println("add " + result + ", $zero ," + mflo);
				freeRegister(mflo);
				break;
			case GT: 
				writer.println("slt " + result + ", " + lhsReg + ", " + rhsReg);
				writer.println("xori " + result + ", 1");
				Register tempReg = getRegister();
				writer.println("slt " + tempReg + ", " + rhsReg + ", " + lhsReg);
				writer.println("and " + result + ", " + result + ", " + tempReg);
				freeRegister(tempReg);
				break;
			case LT: 
				writer.println("slt " + result + ", " + lhsReg + ", " + rhsReg);
				break;
			case GE: 
				writer.println("slt " + result + ", " + lhsReg + ", " + rhsReg);
				Register tempReg2 = getRegister();
				writer.println("li " + tempReg2 + ", 1");
				writer.println("slt " + result + ", " + result + ", " + tempReg2);
				freeRegister(tempReg2);
				break;
			case LE: 
				writer.println("slt " + result + ", " + lhsReg + ", " + rhsReg);
				Register tempReg3 = getRegister();
				writer.println("slt " + tempReg3 + ", " + rhsReg + ", " + lhsReg);
				Register temp = getRegister();
				writer.println("li " + temp + ", 1");
				writer.println("slt " + tempReg3 + ", " + tempReg3 + ", " + temp);
				freeRegister(temp);
				writer.println("or " + result + ", " + result + ", " + tempReg3);
				freeRegister(tempReg3);
				break;
			case NE: 
				writer.println("slt " + result + ", " + lhsReg + ", " + rhsReg);
				Register tempReg4 = getRegister();
				writer.println("slt " + tempReg4 + ", " + rhsReg + ", " + lhsReg);
				writer.println("xor " + result + ", " + result + ", " + tempReg4);
				freeRegister(tempReg4);
				break;
			case EQ: 
				writer.println("slt " + result + ", " + lhsReg + ", " + rhsReg);
				Register tempReg5 = getRegister();
				writer.println("slt " + tempReg5 + ", " + rhsReg + ", " + lhsReg);
				writer.println("or " + result + ", " + result + ", " + tempReg5);
				writer.println("li " + tempReg5 + ", 1");
				writer.println("slt " + result + ", " + result + ", " + tempReg5);
				freeRegister(tempReg5);
				break;
			default:
				break;
			}
			freeRegister(lhsReg);
			freeRegister(rhsReg);
			return result;
		}
	}

	private Register visitBinOp2(BinOp binOp) {
		Register result = getRegister();
		Register lhsReg = binOp.lhs.accept(this);
		int orAndCountTemp = orAndCount;
		if (binOp.op == Op.OR) {
			Register temp = getRegister();
			writer.println("li " + temp + ", 1");
			writer.println("beq " + lhsReg + ", " + temp + ", true" + orAndCountTemp);
			if (binOp.rhs instanceof BinOp) {
				BinOp binTemp = (BinOp) binOp.rhs;
				freeRegister(lhsReg);
				while (binTemp.rhs instanceof BinOp) {
					lhsReg = binTemp.lhs.accept(this);
					writer.println("beq " + lhsReg + ", " + temp + ", true" + orAndCountTemp);
					freeRegister(lhsReg);
					binTemp = (BinOp) binTemp.rhs;
					if (binTemp.op != Op.OR && binTemp.op != Op.AND) {
						Expr rhsTemp = (Expr) binTemp;
						Register rhsReg = rhsTemp.accept(this);
						writer.println("beq " + rhsReg + ", " + temp + ", true" + orAndCountTemp);
						freeRegister(temp);
						freeRegister(rhsReg);
						break;
					}
				}
			}
			else {
				Register rhsReg = binOp.rhs.accept(this);
				writer.println("beq " + rhsReg + ", " + temp + ", true" + orAndCountTemp);
				freeRegister(temp);
				freeRegister(rhsReg);
			}
			writer.println("false" + orAndCountTemp + ":   li " + result + ", 0");
			writer.println("j endBinOp" + orAndCountTemp);
			writer.println("true" + orAndCountTemp + ":   li " + result + ", 1" );
		}
		else {
			writer.println("beq $zero, " + lhsReg + ", false" + orAndCountTemp);
			if (binOp.rhs instanceof BinOp) {
				BinOp binTemp = (BinOp) binOp.rhs;
				freeRegister(lhsReg);
				while (binTemp.rhs instanceof BinOp) {
					lhsReg = binTemp.lhs.accept(this);
					writer.println("beq $zero, " + lhsReg + ", false" + orAndCountTemp);
					freeRegister(lhsReg);
					binTemp = (BinOp) binTemp.rhs;
					if (binTemp.op != Op.OR && binTemp.op != Op.AND) {
						Expr rhsTemp = (Expr) binTemp;
						Register rhsReg = rhsTemp.accept(this);
						writer.println("beq $zero, " + rhsReg + ", false" + orAndCountTemp);
						freeRegister(rhsReg);
						break;
					}
				}
				
			}
			else {
				Register rhsReg = binOp.rhs.accept(this);
				writer.println("beq $zero, " + rhsReg + ", false" + orAndCountTemp);
				freeRegister(rhsReg);
			}
			writer.println("li " + result + ", 1");
			writer.println("j endBinOp" + orAndCountTemp);
			writer.println("false" + orAndCountTemp + ":   li " + result + ", 0");
		}
		writer.println("endBinOp" + orAndCountTemp + ":   ");
		orAndCount++;
		return result;
		// TODO Auto-generated method stub
		
	}

	@Override
	public Register visitAssign(Assign assign) {
		Register rhsReg = getRegister();
		if (assign.rhs instanceof VarExpr) {
			VarExpr ve = (VarExpr) assign.rhs;
			if (ve.vd.type instanceof StructType && assign.lhs instanceof FieldAccessExpr) {
				Register lhsReg = assign.lhs.accept(this);
				writer.println("la " + rhsReg + ", Struct_" + ve.name);
				VarExpr lhsVE = (VarExpr) ((FieldAccessExpr) assign.lhs).structure;
				int count = 0;
				for (String s : structs.get(ve.name)) {
					for (String str : structs.get(lhsVE.name)) {
						if (s == str) {
							writer.println("lw " + rhsReg + ", " + count + "(" + rhsReg + ")");
							writer.println("sw " + rhsReg + ", " + count + "(" + lhsReg + ")");
						}
					}
					count += 4;
				}
				freeRegister(lhsReg);
				freeRegister(rhsReg);
				return null;
			}
			freeRegister(rhsReg);
			rhsReg = assign.rhs.accept(this);
		}
		else if (assign.rhs instanceof TypeCastExpr) {
			TypeCastExpr tce = (TypeCastExpr) assign.rhs;
			if (tce.type instanceof PointerType) {
				if (tce.expr instanceof VarExpr) {
					VarExpr ve = (VarExpr) tce.expr;
					writer.println("addi " + rhsReg + ", $fp, " + ve.vd.stackCount);
					if (assign.lhs instanceof VarExpr) {
						VarExpr v = (VarExpr) assign.lhs;
						Register lhsReg = getRegister();
						if (v.vd.stackCount == -1) writer.println("la " + lhsReg + ", " + v.vd.varName);
				    	else writer.println("la " + lhsReg + ", -" + v.vd.stackCount + "($fp)");
						writer.println("sw " + rhsReg + ", 0(" + lhsReg + ")");
						freeRegister(lhsReg);
					}
					else {
						Register lhsReg = assign.lhs.accept(this);
						writer.println("sw " + rhsReg + ", 0(" + lhsReg + ")");
						freeRegister(lhsReg);
					}
					freeRegister(rhsReg);
					return null;
				}
				else {
					freeRegister(rhsReg);
					rhsReg = tce.expr.accept(this);
				}
			}
		}
		else {
			freeRegister(rhsReg);
			rhsReg = assign.rhs.accept(this);
		}
		if (assign.rhs instanceof ArrayAccessExpr) writer.println("lw " + rhsReg + ", 0(" + rhsReg + ")");
		if (assign.rhs instanceof FieldAccessExpr) writer.println("lw " + rhsReg + ", 0(" + rhsReg + ")");
		Register lhsReg = null;
		if (assign.lhs instanceof VarExpr) {
			lhsReg = getRegister();
			VarExpr v = (VarExpr) assign.lhs;
			if (v.vd.stackCount == -1) {
	    		writer.println("la " + lhsReg + ", " + v.vd.varName);
	    	}
			else {
				writer.println("la " + lhsReg + ", -" + v.vd.stackCount + "($fp)");
			}
		}
		else if (assign.lhs instanceof ArrayAccessExpr) {
			ArrayAccessExpr a = (ArrayAccessExpr) assign.lhs;
			lhsReg = a.accept(this);
		}
		else if (assign.lhs instanceof FieldAccessExpr) {
			FieldAccessExpr f = (FieldAccessExpr) assign.lhs;
			lhsReg = f.accept(this);
		}
		else if (assign.lhs instanceof ValueAtExpr) {
			ValueAtExpr f = (ValueAtExpr) assign.lhs;
			lhsReg = f.value.accept(this);
			writer.println("la " + lhsReg + ", 0(" + lhsReg + ")");
		}
		writer.println("sw " + rhsReg + ", 0(" + lhsReg + ")");
		freeRegister(lhsReg);
		freeRegister(rhsReg);
		return null;
	}

	@Override
	public Register visitChrLiteral(ChrLiteral chrLiteral) {
		Register result = getRegister();
		writer.println("li " + result + ", \'" + chrLiteral.value + "\'");
		return result;
	}

	@Override
	public Register visitArrayType(ArrayType arrayType) {
		Register result = getRegister();
		writer.println("li " + result + ", " + (4 * arrayType.numberOfElements));
		return result;
	}

	@Override
	public Register visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
		Register result = getRegister();
		if (!(arrayAccessExpr.array.type instanceof PointerType)) {
			Register address = getRegister();
			writer.println("la " + address + ", " + arrayAccessExpr.vd.varName);
			if (arrayAccessExpr.index instanceof IntLiteral) {
				IntLiteral n = (IntLiteral) arrayAccessExpr.index;
				writer.println("la " + result + ", " + (n.value * 4) + "(" + address + ")");
			}
			else {
				if (arrayAccessExpr.index instanceof VarExpr || arrayAccessExpr.index instanceof ArrayAccessExpr || arrayAccessExpr.index instanceof FunCallExpr);
				Register index = arrayAccessExpr.index.accept(this);
				writer.println("mul " + index + ", " + index + ", 4");
				writer.println("add " + address + ", " + address + ", " + index);
				freeRegister(index);
				writer.println("la " + result + ", 0(" + address + ")");
			}
	    	
			freeRegister(address);
		}
		else {
			Register temp = arrayAccessExpr.array.accept(this);
			writer.println("la " + result + ", " + arrayAccessExpr.index + "(" + temp + ")");
			freeRegister(temp);
		}
		return result;
	}

	@Override
	public Register visitStructType(StructType structType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Register visitTypeCastExpr(TypeCastExpr typeCastExpr) {
		if (typeCastExpr.expr instanceof VarExpr) {
			VarExpr ve = (VarExpr) typeCastExpr.expr;
			Register result = getRegister();
			writer.println("addi " + result + ", $fp, -" + ve.vd.stackCount);
			return result;
		}
		return typeCastExpr.expr.accept(this);
	}

	@Override
	public Register visitValueAtExpr(ValueAtExpr valueAtExpr) {
		Register result = valueAtExpr.value.accept(this);
		writer.println("la " + result + ", 0(" + result + ")");
		return result;
	}

	@Override
	public Register visitWhile(While while1) {
		loopCount++;
		int loopCountTemp = loopCount;
		writer.println("loopStart" + loopCountTemp + ":   ");
		Register exprReg = while1.expr.accept(this);
		writer.println("beq $zero, " + exprReg + ", loopEnd" + loopCountTemp);
		freeRegister(exprReg);
		while1.stmt.accept(this);
		writer.println("j loopStart" + loopCountTemp);
		writer.println("loopEnd" + loopCountTemp + ":   ");		
		return null;
	}

	@Override
	public Register visitFunCallExpr(FunCallExpr funCallExpr) {
		//Register result isnt freed when temp is!!!!!!!
		if (funCallExpr.name.equals("print_i")) {
			Register temp = funCallExpr.exprs.get(0).accept(this);
			if (funCallExpr.exprs.get(0) instanceof ArrayAccessExpr || funCallExpr.exprs.get(0) instanceof FieldAccessExpr) writer.println("lw " + temp + ", 0(" + temp + ")");
			writer.println("li $v0, 1");
			writer.println("add $a0, $zero, " + temp);
			writer.println("syscall");
			freeRegister(temp);
		}
		else if (funCallExpr.name.equals("print_s")) {
			writer.println("li $v0, 4");
			if (funCallExpr.exprs.get(0) instanceof TypeCastExpr) {
				TypeCastExpr tce = (TypeCastExpr) funCallExpr.exprs.get(0);
				if (tce.expr instanceof VarExpr) {
					VarExpr v = (VarExpr) tce.expr;
			    	writer.println("la $a0 , " + v.vd.varName);
				}
				else {
					writer.println("la $a0, str" + strCount);
					strCount++;
				}
			}
			else if (funCallExpr.exprs.get(0) instanceof VarExpr) {
				VarExpr v = (VarExpr) funCallExpr.exprs.get(0);
		    	if (v.vd.stackCount == -1) writer.println("la $a0 , " + v.vd.varName);
		    	else writer.println("la $a0, -" + v.vd.stackCount + "($fp)");
			}
			writer.println("syscall");
			
		}
		else if (funCallExpr.name.equals("print_c")) {
			Register temp = funCallExpr.exprs.get(0).accept(this);
			if (funCallExpr.exprs.get(0) instanceof ArrayAccessExpr) writer.println("lw " + temp + ", 0(" + temp + ")");
			writer.println("li $v0, 11");
			writer.println("add $a0, $zero, " + temp);
			writer.println("syscall");
			freeRegister(temp);
		}
		else if (funCallExpr.name.equals("read_i")) {
			writer.println("li $v0, 5");
			writer.println("syscall");
			Register temp = getRegister();
			writer.println("add " + temp + ", $v0, $zero");
			return temp;
		}
		else if (funCallExpr.name.equals("read_c")) {
			writer.println("li $v0, 12");
			writer.println("syscall");
			Register temp = getRegister();
			writer.println("add " + temp + ", $v0, $zero");
			return temp;
		}
		else if (funCallExpr.name.equals("mcmalloc")) {
			Register temp = getRegister();
			writer.println("la " + temp + ", malloc" + mallocCount);
			mallocCount++;
			return temp;
		}
		else {
			writer.println("sw $fp, 0($sp)");
			writer.println("sw $ra, -4($sp)");
			writer.println("addi $sp, $sp, -8");
			Register address = getRegister();
			writer.println("la " + address + ", 0($sp)");
			for (Expr e : funCallExpr.exprs) {
				Register temp = e.accept(this);
				if (e instanceof ArrayAccessExpr || e instanceof FieldAccessExpr) writer.println("lw " + temp + ", 0(" + temp + ")");
				writer.println("sw " + temp + ", 0($sp)");
				writer.println("addi $sp, $sp, -4");
				freeRegister(temp);
			}
			writer.println("add $fp, $zero, " + address);
			freeRegister(address);
			writer.println("jal " + funCallExpr.name);
			writer.println("add $sp, $fp, $zero");
			writer.println("lw $ra, 4($sp)");
			writer.println("lw $fp, 8($sp)");
			writer.println("addi $sp, $sp, 8");
			if (funCallExpr.fd.type != BaseType.VOID) {
				Register returnValue = getRegister();
				writer.println("add " + returnValue + ", $zero, $v0");
				return returnValue;
			}
		}
		return null;
	}

	@Override
	public Register visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
		Register result = getRegister();
		Register address = getRegister();
		VarExpr ve = null;
		if (fieldAccessExpr.structure instanceof ValueAtExpr) {
			ValueAtExpr vae = (ValueAtExpr) fieldAccessExpr.structure;
			if (vae.value instanceof VarExpr) {
				ve = (VarExpr) vae.value;
				writer.println("la " + address + ", Struct_" + ve.name);
				int n = 0;
				//System.out.println("START " + ve.name);
				//System.out.println(fieldAccessExpr.name);
		    	for (String vd : structs.get(ve.name)) {
		    		if (fieldAccessExpr.name.equals(vd)) {
		    			writer.println("la " + result + ", " + (n * 4) + "(" + address + ")");
		    			//System.out.println("h");
		    		}
		    		n++;
		    	}
				freeRegister(address);
			}		
		}
		else if (fieldAccessExpr.structure instanceof VarExpr) {
			ve = (VarExpr) fieldAccessExpr.structure;
			writer.println("la " + address + ", Struct_" + ve.name);
			int n = 0;
//			System.out.println("START " + ve.name);
//			System.out.println(fieldAccessExpr.name);
	    	for (String vd : structs.get(ve.name)) {
	    		//System.out.println(vd);
	    		if (fieldAccessExpr.name.equals(vd)) {
	    			writer.println("la " + result + ", " + (n * 4) + "(" + address + ")");
	    		}
	    		n++;
	    	}
			freeRegister(address);
		}
		
		return result;
	}
	
	
}
