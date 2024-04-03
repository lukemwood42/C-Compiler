package parser;

import ast.*;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * @author cdubach
 */
public class Parser {

    private Token token;

    // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
    private Queue<Token> buffer = new LinkedList<>();

    private final Tokeniser tokeniser;

    public Parser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }

    public Program parse() {
        // get the first token
        nextToken();

        return parseProgram();
    }

    public int getErrorCount() {
        return error;
    }

    private int error = 0;
    private Token lastErrorToken;

    private void error(TokenClass... expected) {

        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected ("+sb+") found ("+token+") at "+token.position);

        error++;
        lastErrorToken = token;
    }

    /*
     * Look ahead the i^th element from the stream of token.
     * i should be >= 1
     */
    private Token lookAhead(int i) {
        // ensures the buffer has the element we want to look ahead
        while (buffer.size() < i)
            buffer.add(tokeniser.nextToken());
        assert buffer.size() >= i;

        int cnt=1;
        for (Token t : buffer) {
            if (cnt == i)
                return t;
            cnt++;
        }

        assert false; // should never reach this
        return null;
    }


    /*
     * Consumes the next token from the tokeniser or the buffer if not empty.
     */
    private void nextToken() {
        if (!buffer.isEmpty())
            token = buffer.remove();
        else
            token = tokeniser.nextToken();
    }

    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     * Returns the expected token or null if an error occurred.
     */
    private Token expect(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
            	//System.out.println(token.toString());
            	Token cur = token;
                nextToken();
                return cur;
            }
        }
        error(expected);
        return null;
    }

    /*
    * Returns true if the current token is equals to any of the expected ones.
    */
    private boolean accept(TokenClass... expected) {
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == token.tokenClass);
        return result;
    }


    private Program parseProgram() {
        parseIncludes();
        List<StructTypeDecl> stds = parseStructDecls(new ArrayList<>());
        List<VarDecl> vds = parseVarDecls(new ArrayList<>());
        List<FunDecl> fds = parseFunDecls(new ArrayList<>());
        expect(TokenClass.EOF);

        return new Program(stds, vds, fds);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

//<<<<<<< HEAD
    private List<StructTypeDecl> parseStructDecls(List<StructTypeDecl> sds) {
    	if (accept(TokenClass.STRUCT) && (lookAhead(3).tokenClass != TokenClass.LPAR && lookAhead(4).tokenClass != TokenClass.LPAR && lookAhead(2).tokenClass == TokenClass.LBRA)) {
	    	nextToken();
	    	String name = token.data;
	        expect(TokenClass.IDENTIFIER);
	        expect(TokenClass.LBRA);
//	        List<VarDecl> vds = new ArrayList<>();
//	        if (accept(TokenClass.STRUCT)) {
//				nextToken();
//				expect(TokenClass.IDENTIFIER);
//			}
//			else {expect(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID);}
//			parseAstr();
//    		expect(TokenClass.IDENTIFIER);
//			if (accept(TokenClass.LSBR)) {
//				nextToken();
//				expect(TokenClass.INT_LITERAL);
//				expect(TokenClass.RSBR);
//			}
//			expect(TokenClass.SC);
//				
			List<VarDecl> vds = parseVarDecls(new ArrayList<>());	
			if (vds.isEmpty()) {
				System.out.println("Structtype decl params are empty");
				error++;
			}
	        expect(TokenClass.RBRA);
	        expect(TokenClass.SC);
	        StructType st = new StructType(name);
	        StructTypeDecl sd = new StructTypeDecl(st, vds);
	        sds.add(sd);
	        return parseStructDecls(sds);
        }
        return sds;
            
    }
        		
    private List<VarDecl> parseVarDecls(List<VarDecl> vds) {  
    	//can declare vars after functions!!!!!!!!!!!
    	if (accept(TokenClass.INT, TokenClass.VOID, TokenClass.CHAR, TokenClass.STRUCT) && (lookAhead(2).tokenClass != TokenClass.LPAR && lookAhead(1).tokenClass != TokenClass.ASSIGN)) {
    		VarDecl vd = null;
    		try {
    			vd = (VarDecl) parseVarFunDecls();
    		}
    		catch (ClassCastException e) {
    			System.out.println("error");
    		}
    		vds.add(vd);
    		return parseVarDecls(vds);
    	}
    	return vds;
    }
    
    private Object parseVarFunDecls() {
    	Type type = parseType();
    	String ident = token.data;
    	expect(TokenClass.IDENTIFIER);
    	if (accept(TokenClass.LPAR)) {
    		FunDecl fd = parseFunDecls2(type, ident);
    		return fd;
    	}
    	else {
    		if (accept(TokenClass.LSBR)) {
    			nextToken();
    			int n = Integer.parseInt(token.data);
    			expect(TokenClass.INT_LITERAL);
    			expect(TokenClass.RSBR);
    			type = new ArrayType(type, n);
    		}
    	    expect(TokenClass.SC);
    	    VarDecl vd = new VarDecl(type, ident, 0);
    		return vd;
    	}
    }
    
    private FunDecl parseFunDecls2(Type type, String ident) {
    	expect(TokenClass.LPAR);
    	List<VarDecl> vds = new ArrayList<>();
		if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)) {
			Type t = parseType();
			String name = token.data;
			expect(TokenClass.IDENTIFIER);
			if (accept(TokenClass.LSBR)) {
    			nextToken();
    			int n = Integer.parseInt(token.data);
    			expect(TokenClass.INT_LITERAL);
    			expect(TokenClass.RSBR);
    			t = new ArrayType(t, n);
    		}
			vds.add(new VarDecl(t, name, 0));	
			while (accept(TokenClass.COMMA)) {
				nextToken();
				t = parseType();
				name = token.data;
				expect(TokenClass.IDENTIFIER);
				if (accept(TokenClass.LSBR)) {
	    			nextToken();
	    			int n = Integer.parseInt(token.data);
	    			expect(TokenClass.INT_LITERAL);
	    			expect(TokenClass.RSBR);
	    			t = new ArrayType(t, n);
	    		}
				vds.add(new VarDecl(t, name, 0));
			}
		}
    	expect(TokenClass.RPAR);
		Block block = parseBlock();
		FunDecl fd = new FunDecl(type, ident, vds, block);
		return fd;
    }
    	
    
    private List<FunDecl> parseFunDecls(List<FunDecl> fds) {
    	if (accept(TokenClass.INT, TokenClass.VOID, TokenClass.CHAR, TokenClass.STRUCT) && (lookAhead(1).tokenClass != TokenClass.ASSIGN)) {
    		FunDecl fd = null;
    		try {
    			fd = (FunDecl) parseVarFunDecls();
    		}
    		catch (ClassCastException e) {
    			System.out.println("error");
    		}
    		fds.add(fd);
    		return parseFunDecls(fds);
    	}
    	return fds;
    }
    
    private Type parseType() {
    	if ((accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID) && lookAhead(1).tokenClass == TokenClass.ASTERIX) || (accept(TokenClass.STRUCT) && lookAhead(2).tokenClass == TokenClass.ASTERIX)) {
    		Type type = parseType2();
    		parseAstr();
    		return new PointerType(type);
    	}
		return parseType2();
    }
    	
    private Type parseType2() {
    	if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID)) {  		
    		BaseType bt = null;
    		if (token.tokenClass == TokenClass.INT) bt = BaseType.INT;
    		else if (token.tokenClass == TokenClass.CHAR) bt = BaseType.CHAR;
    		else if (token.tokenClass == TokenClass.VOID) bt = BaseType.VOID;
    		nextToken();
    		parseAstr();
    		return bt;
    	}
    	else  {
    		expect(TokenClass.STRUCT);
    		String name = token.data;
    		expect(TokenClass.IDENTIFIER);
    		parseAstr();
    		return new StructType(name);
    	}
    }
    	
//    	
//    	if (accept(TokenClass.STRUCT)) {
//			nextToken();
//			expect(TokenClass.IDENTIFIER);
//		}
//		else {expect(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID);}
//		parseAstr();
    
    
    private void parseAstr() {
    	if (accept(TokenClass.ASTERIX)) {
    		nextToken();
    	}
    }
 
  
    private Block parseBlock() {
    	expect(TokenClass.LBRA);
    	//System.out.println(token.toString() + "block");
    	List<VarDecl> varDecls = parseVarDecls(new ArrayList<>());
    	List<Stmt> stmts = new ArrayList<>();
    	//delete to go back
    	//System.out.println(token.toString());
    //	System.out.println("next - " + token.toString());
    
    	while (accept(TokenClass.WHILE, TokenClass.LBRA, TokenClass.IF, TokenClass.RETURN, TokenClass.LPAR, TokenClass.IDENTIFIER, TokenClass.MINUS, TokenClass.ASTERIX, TokenClass.SIZEOF, TokenClass.CHAR_LITERAL, TokenClass.INT_LITERAL, TokenClass.STRING_LITERAL)) {
    		stmts.add(parseStmt());
    	}
    	expect(TokenClass.RBRA);
    	return new Block(varDecls, stmts);
    }
    
    private Stmt parseStmt() {
    	if (accept(TokenClass.LBRA)) {
    		//System.out.println(token.toString() + "stmt");
    		Block block = parseBlock();
    		return block;
    		//remove this to fix block
//    		if (accept(TokenClass.RBRA)) {
//    			return parseStmt(stmts);
//    		}
    	}
    	else if (accept(TokenClass.WHILE)) {
    		nextToken();
    		expect(TokenClass.LPAR);
    		Expr expr = parseExp();
    		expect(TokenClass.RPAR);
	    	Stmt temp = parseStmt();
	    	Stmt whileStmt = new While(expr, temp);
	    	return whileStmt;
    		
    	}
    	else if (accept(TokenClass.IF)) {
    		nextToken();
    		expect(TokenClass.LPAR);
	    	Expr expr = parseExp();
	    	expect(TokenClass.RPAR);
	    	Stmt stmt1 = parseStmt();
	    	Stmt stmt2 = null;
	    	if (accept(TokenClass.ELSE)) {
	    		nextToken();
	    		stmt2 = parseStmt();
	    	}
	    	Stmt ifStmt = new If(expr, stmt1, stmt2);
	    	return ifStmt;
	    	
	    }	    		
    	else if (accept(TokenClass.RETURN)) {
    		nextToken();
    		Expr expr = null;
    		if (!accept(TokenClass.SC)) {
    			expr = parseExp();
    		}
    		expect(TokenClass.SC);
    		Stmt returnStmt = new Return(expr);
    		return returnStmt;
    	}
    	else if (accept(TokenClass.LPAR, TokenClass.IDENTIFIER, TokenClass.MINUS, TokenClass.ASTERIX, 
    							TokenClass.SIZEOF, TokenClass.CHAR_LITERAL, TokenClass.INT_LITERAL, TokenClass.STRING_LITERAL))	 { //, TokenClass.ELSE, TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)){
//    		System.out.println("before exp");
    		Expr lhs = parseExp();
    		////system.out.println("after exp");
    		Stmt stmt = null;
    		if (accept(TokenClass.ASSIGN)) {
    			nextToken();
    			Expr rhs = parseExp();
    			stmt = new Assign(lhs, rhs);
    		}
    		else {
    			stmt = new ExprStmt(lhs);
    		}
    		//System.out.println("after stmt");
    		expect(TokenClass.SC);
    		return stmt;
    	}
		return null;
    }
    
    
    private Expr parseExp() {
    	Expr lhs = parseExp2();
    	if (accept(TokenClass.OR)) {
    		Op op = Op.OR;
    		nextToken();
    		Expr rhs = parseExp();
    		return new BinOp(op, lhs, rhs);
    	}
    	return lhs;
    }
    
    private Expr parseExp2() {
    	Expr lhs = parseExp3();
    	if (accept(TokenClass.AND)) {
    		Op op = Op.AND;
    		nextToken();
    		Expr rhs = parseExp2();
    		return new BinOp(op, lhs, rhs);
    	}
    	return lhs;
    }
    
    private Expr parseExp3() {
    	Expr lhs = parseExp4();
	    if (accept(TokenClass.EQ, TokenClass.NE)) {
	    	Op op;
	    	if (token.tokenClass == TokenClass.EQ) op = Op.EQ;
	    	else op = Op.NE;
	    	nextToken();
	    	Expr rhs = parseExp3();	 
	    	return new BinOp(op, lhs, rhs);
	    }
	    return lhs;
    }
    
    
    private Expr parseExp4() {
    	Expr lhs = parseExp5();
    	if (accept(TokenClass.GT, TokenClass.LT, TokenClass.GE, TokenClass.LE)) {
    		Op op;
    		if (token.tokenClass == TokenClass.GT) op = Op.GT;
	    	else if (token.tokenClass == TokenClass.LT) op = Op.LT;
	    	else if (token.tokenClass == TokenClass.GE) op = Op.GE;
	    	else op = Op.LE;
    		nextToken();
    		Expr rhs = parseExp4();
    		return new BinOp(op, lhs, rhs);
    	}
    	return lhs;
    }
    
    private Expr parseExp5() {
    	Expr lhs = parseExp6();
    	if (accept(TokenClass.PLUS, TokenClass.MINUS)) {
    		Op op;
	    	if (token.tokenClass == TokenClass.PLUS) op = Op.ADD;
	    	else op = Op.SUB;
	    	nextToken();
    		Expr rhs = parseExp5();
    		return new BinOp(op, lhs, rhs);
    	}
    	return lhs;
    }
    
    private Expr parseExp6() {
    	Expr lhs = parseExp7();
    	if (accept(TokenClass.ASTERIX, TokenClass.DIV, TokenClass.REM)) {
    		Op op;
    		if (token.tokenClass == TokenClass.ASTERIX) op = Op.MUL;
	    	else if (token.tokenClass == TokenClass.DIV) op = Op.DIV;
	    	else op = Op.MOD;
    		nextToken();
    		Expr rhs = parseExp6();
    		return new BinOp(op, lhs, rhs);
    	}
    	return lhs;
    }
    
    private Expr parseExp7() {
    	if (accept(TokenClass.MINUS, TokenClass.ASTERIX)) {
    		if (accept(TokenClass.MINUS)) {
        		Expr lhs;
        		Op op;
        		Expr rhs;
    			nextToken();
    			lhs = new IntLiteral(0);
    			op = Op.SUB;
    			rhs = parseExp7();
    			return new BinOp(op, lhs, rhs);
    		}
    		else {
    			nextToken();
    			Expr rhs = parseExp();
    			return new ValueAtExpr(rhs);
    		}
    	}
    	else if (accept(TokenClass.SIZEOF)) {
    		nextToken();
    		expect(TokenClass.LPAR);
    		Type rhs = parseType();
    		expect(TokenClass.RPAR);
    		return new SizeOfExpr(rhs);
    	}
    	else if (accept(TokenClass.LPAR) && (lookAhead(1).tokenClass == TokenClass.INT || lookAhead(1).tokenClass == TokenClass.CHAR || lookAhead(1).tokenClass == TokenClass.VOID || lookAhead(1).tokenClass == TokenClass.STRUCT)) {
    		nextToken();
    		Type type = parseType();
    		expect(TokenClass.RPAR);
    		Expr rhs = parseExp();
    		return new TypeCastExpr(type, rhs);
    	}
    	else {return parseExp8();}
    	
    }
    private Expr parseExp8() {
    	//system.out.println("start exp8");
    	if (accept(TokenClass.LPAR)) {
    		nextToken();
    		Expr expr = parseExp();
    		expect(TokenClass.RPAR);
    		return parseExp9(expr);	
    	}
    	else if (accept(TokenClass.IDENTIFIER) && lookAhead(1).tokenClass == TokenClass.LPAR) {
    		String string = token.data;
    		nextToken();
    		nextToken();
    		List<Expr> exprs = new ArrayList<>();
    		//system.out.println(token.toString());
    		if (!accept(TokenClass.RPAR)) {exprs.add(parseExp());}
    		while (accept(TokenClass.COMMA)) {
    			nextToken();
    			exprs.add(parseExp());
    		}
    		expect(TokenClass.RPAR);
    		Expr expr = new FunCallExpr(string, exprs);
    		//system.out.println("after exp8");
    		return parseExp9(expr);
    	}
    	else {
    		Expr expr = null;
    		if (token.tokenClass == TokenClass.CHAR_LITERAL) expr = new ChrLiteral(token.data);
	    	else if (token.tokenClass == TokenClass.INT_LITERAL) expr = new IntLiteral(Integer.parseInt(token.data));
	    	else if (token.tokenClass == TokenClass.STRING_LITERAL) expr = new StrLiteral(token.data);
	    	else expr = new VarExpr(token.data);
    		expect(TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL);
    		return parseExp9(expr);
    	}
    }
    
    private Expr parseExp9(Expr expr) {
    	if (accept(TokenClass.LSBR)) {
    		nextToken();
    		Expr rhs = parseExp();
    		expect(TokenClass.RSBR);
    		Expr e = new ArrayAccessExpr(expr, rhs);
    		return parseExp9(e);
    	}
    	else if (accept(TokenClass.DOT)) {
    		nextToken();
    		String s = token.data;
    		expect(TokenClass.IDENTIFIER);
    		Expr e = new FieldAccessExpr(expr, s);
    		return parseExp9(e);
    	}
    	else {
    		return expr;
    	}
    }
//=======
//    private List<StructTypeDecl> parseStructDecls() {
//        // to be completed ...
//        return null;
//    }
//
//    private List<VarDecl> parseVarDecls() {
//        // to be completed ...
//        return null;
//    }
//
//    private List<FunDecl> parseFunDecls() {
//        // to be completed ...
//        return null;
//>>>>>>> 189b9885577b33edc5be7202d9d65f085baf2b9b
//    }

}
