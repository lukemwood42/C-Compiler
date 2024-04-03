package parser;

import ast.*;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
* @author cdubach
*/
public class ParserPart1 {

   private Token token;

   // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
   private Queue<Token> buffer = new LinkedList<>();

   private final Tokeniser tokeniser;



   public ParserPart1(Tokeniser tokeniser) {
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
           	System.out.println(token.toString());
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
       List<StructTypeDecl> stds = null;//parseStructDecls();
       List<VarDecl> vds = null;//parseVarDecls();
       List<FunDecl> fds = null;//parseFunDecls();
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
//
//<<<<<<< HEAD
   private void parseStructDecls() {
       if (accept(TokenClass.STRUCT) && (lookAhead(3).tokenClass != TokenClass.LPAR && lookAhead(4).tokenClass != TokenClass.LPAR)) {
	    	nextToken();
	        expect(TokenClass.IDENTIFIER);
	        expect(TokenClass.LBRA);
   		parseType();
   		expect(TokenClass.IDENTIFIER);
			if (accept(TokenClass.LSBR)) {
				nextToken();
				expect(TokenClass.INT_LITERAL);
				expect(TokenClass.RSBR);
			}
			expect(TokenClass.SC);

			parseVarDecls();
	        expect(TokenClass.RBRA);
	        expect(TokenClass.SC);
	        parseStructDecls();
       }

   }

   private void parseVarDecls() {
   	if (accept(TokenClass.INT, TokenClass.VOID, TokenClass.CHAR, TokenClass.STRUCT)) {
   		parseVarFunDecls();
   	}

	    if ((lookAhead(1).tokenClass == TokenClass.IDENTIFIER && lookAhead(2).tokenClass != TokenClass.LPAR) || (lookAhead(2).tokenClass == TokenClass.IDENTIFIER && lookAhead(3).tokenClass != TokenClass.LPAR) || (lookAhead(3).tokenClass == TokenClass.IDENTIFIER && lookAhead(4).tokenClass != TokenClass.LPAR)){
	    	System.out.println("varDecl");
	    	if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)) {
	    		if (accept(TokenClass.STRUCT)) {
	    			parseStructDecls();
	    			parseVarDecls();
	    		}
	    		else {
		    		expect(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID);
		    		parseAstr();
		    		expect(TokenClass.IDENTIFIER);
		    		if (accept(TokenClass.LSBR)) {
		    			nextToken();
		    			expect(TokenClass.INT_LITERAL);
		    			expect(TokenClass.RSBR);
		    		}
		    	    expect(TokenClass.SC);
		    		parseVarDecls();
	    		}
	    	}
	    }
   }

   private void parseVarFunDecls() {
   	parseType();
   	expect(TokenClass.IDENTIFIER);
   	if (accept(TokenClass.LPAR)) {
   		parseFunDecls2();
   	}
   	else {
   		if (accept(TokenClass.LSBR)) {
   			nextToken();
   			expect(TokenClass.INT_LITERAL);
   			expect(TokenClass.RSBR);
   		}
   	    expect(TokenClass.SC);
   		parseVarDecls();
   	}
   }

   private void parseFunDecls2() {
   	expect(TokenClass.LPAR);
		if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)) {
			parseType();
   		expect(TokenClass.IDENTIFIER);
			while (accept(TokenClass.COMMA)) {
				nextToken();
				parseType();
				expect(TokenClass.IDENTIFIER);
			}
		}
   	expect(TokenClass.RPAR);
		parseBlock();
		parseFunDecls();
   }


   private void parseFunDecls() {
   	if (accept(TokenClass.INT, TokenClass.VOID, TokenClass.CHAR, TokenClass.STRUCT)) {
   		parseVarFunDecls();
   	}
//
//    	if ((lookAhead(1).tokenClass == TokenClass.IDENTIFIER && lookAhead(2).tokenClass == TokenClass.LPAR) || (lookAhead(2).tokenClass == TokenClass.IDENTIFIER && lookAhead(3).tokenClass == TokenClass.LPAR || (lookAhead(3).tokenClass == TokenClass.IDENTIFIER && lookAhead(4).tokenClass == TokenClass.LPAR))) {
//    		System.out.println("error");
//    		parseType();
//    		expect(TokenClass.IDENTIFIER);
//    		expect(TokenClass.LPAR);
//    		if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)) {
//    			parseType();
//        		expect(TokenClass.IDENTIFIER);
//    			while (accept(TokenClass.COMMA)) {
//    				nextToken();
//    				parseType();
//    				expect(TokenClass.IDENTIFIER);
//    			}
//    		}
//	    	expect(TokenClass.RPAR);
//    		parseBlock();
//    		parseFunDecls();
//
//    	}
   }

   private void parseType() {
   	if (accept(TokenClass.STRUCT)) {
			nextToken();
			expect(TokenClass.IDENTIFIER);
		}
		else {expect(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID);}
		parseAstr();
   }

   private void parseAstr() {
   	if (accept(TokenClass.ASTERIX)) {
   		nextToken();
   	}
   }


   private void parseBlock() {
   	expect(TokenClass.LBRA);
   	System.out.println(token.toString());
		parseVarDecls();
		parseStmt();
		expect(TokenClass.RBRA);
   }

   private void parseStmt() {
   	if (accept(TokenClass.LBRA)) {
   		parseBlock();
   		parseStmt();
   	}
   	else if (accept(TokenClass.WHILE)) {
   		nextToken();
   		expect(TokenClass.LPAR);
   		parseExp();
   		expect(TokenClass.RPAR);
   		parseStmt();
   		parseStmt();
   	}
   	else if (accept(TokenClass.IF)) {
   		nextToken();
   		expect(TokenClass.LPAR);
	    	parseExp();
	    	expect(TokenClass.RPAR);
		    parseStmt();
		    if (accept(TokenClass.ELSE)) {
		    	nextToken();
		    	parseStmt();
		    }
		    parseStmt();
	    }

   	else if (accept(TokenClass.RETURN)) {
   		nextToken();
   		if (!accept(TokenClass.SC)) {
   			parseExp();
   		}
   		expect(TokenClass.SC);
   		parseStmt();
   	}
   	else if (accept(TokenClass.LPAR, TokenClass.IDENTIFIER, TokenClass.MINUS, TokenClass.ASTERIX,
   							TokenClass.SIZEOF, TokenClass.CHAR_LITERAL, TokenClass.INT_LITERAL, TokenClass.STRING_LITERAL))	 { //, TokenClass.ELSE, TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)){

   		parseExp();
   		if (accept(TokenClass.ASSIGN)) {
   			nextToken();
   			parseExp();
   		}
   		expect(TokenClass.SC);
   		parseStmt();
   	}
   }


   private void parseExp() {
   	parseExp2();
   	while (accept(TokenClass.OR)) {
   		nextToken();
   		parseExp();
   	}
   }

   private void parseExp2() {
   	parseExp3();
   	while (accept(TokenClass.AND)) {
   		nextToken();
   		parseExp2();
   	}
   }

   private void parseExp3() {
   	parseExp4();
	    while (accept(TokenClass.EQ, TokenClass.NE)) {
	    	nextToken();
	    	parseExp3();
	    }
   }


   private void parseExp4() {
   	parseExp5();
   	while (accept(TokenClass.GT, TokenClass.LT, TokenClass.GE, TokenClass.LE)) {
   		nextToken();
   		parseExp4();
   	}
   }

   private void parseExp5() {
   	parseExp6();
   	while (accept(TokenClass.PLUS, TokenClass.MINUS)) {
   		nextToken();
   		parseExp5();
   	}
   }

   private void parseExp6() {
   	parseExp7();
   	while (accept(TokenClass.ASTERIX, TokenClass.DIV, TokenClass.REM)) {
   		nextToken();
   		parseExp6();
   	}
   }

   private void parseExp7() {
   	if (accept(TokenClass.MINUS, TokenClass.ASTERIX)) {
   		nextToken();
   		parseExp();
   	}
   	else if (accept(TokenClass.SIZEOF)) {
   		nextToken();
   		expect(TokenClass.LPAR);
   		parseType();
   		expect(TokenClass.RPAR);
   	}
   	else if (accept(TokenClass.LPAR) && (lookAhead(1).tokenClass == TokenClass.INT || lookAhead(1).tokenClass == TokenClass.CHAR || lookAhead(1).tokenClass == TokenClass.VOID || lookAhead(1).tokenClass == TokenClass.STRUCT)) {
   		nextToken();
   		parseType();
   		expect(TokenClass.RPAR);
   		parseExp();
   	}
   	else {parseExp8();}

   }
   private void parseExp8() {
   	if (accept(TokenClass.LPAR)) {
   		nextToken();
   		parseExp();
   		expect(TokenClass.RPAR);
   		parseExp9();
   	}
   	else if (accept(TokenClass.IDENTIFIER) && lookAhead(1).tokenClass == TokenClass.LPAR) {
   		nextToken();
   		nextToken();
   		if (!accept(TokenClass.RPAR)) {parseExp();}
   		while (accept(TokenClass.COMMA)) {
   			nextToken();
   			parseExp();
   		}
   		expect(TokenClass.RPAR);
   		parseExp9();
   	}
   	else {
   		expect(TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL, TokenClass.IDENTIFIER, TokenClass.INT_LITERAL);
   		parseExp9();
   	}
   }

   private void parseExp9() {
   	if (accept(TokenClass.LSBR)) {
   		nextToken();
   		parseExp();
   		expect(TokenClass.RSBR);
   		parseExp9();
   	}
   	else if (accept(TokenClass.DOT)) {
   		nextToken();
   		expect(TokenClass.IDENTIFIER);
   		parseExp9();
   	}
   }

}




//package parser;
//
//import lexer.Token;
//import lexer.Tokeniser;
//import lexer.Token.TokenClass;
//
//import java.util.LinkedList;
//import java.util.Queue;
//
//
///**
// * @author cdubach
// */
//public class ParserPart1 {
//
//    private Token token;
//
//    // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
//    private Queue<Token> buffer = new LinkedList<>();
//
//    private final Tokeniser tokeniser;
//
//
//
//    public ParserPart1(Tokeniser tokeniser) {
//        this.tokeniser = tokeniser;
//    }
//
//    public void parse() {
//        // get the first token
//        nextToken();
//
//        parseProgram();
//    }
//
//    public int getErrorCount() {
//        return error;
//    }
//
//    private int error = 0;
//    private Token lastErrorToken;
//
//    private void error(TokenClass... expected) {
//
//        if (lastErrorToken == token) {
//            // skip this error, same token causing trouble
//            return;
//        }
//
//        StringBuilder sb = new StringBuilder();
//        String sep = "";
//        for (TokenClass e : expected) {
//            sb.append(sep);
//            sb.append(e);
//            sep = "|";
//        }
//        System.out.println("Parsing error: expected ("+sb+") found ("+token+") at "+token.position);
//
//        error++;
//        lastErrorToken = token;
//    }
//
//    /*
//     * Look ahead the i^th element from the stream of token.
//     * i should be >= 1
//     */
//    private Token lookAhead(int i) {
//        // ensures the buffer has the element we want to look ahead
//        while (buffer.size() < i)
//            buffer.add(tokeniser.nextToken());
//        assert buffer.size() >= i;
//
//        int cnt=1;
//        for (Token t : buffer) {
//            if (cnt == i)
//                return t;
//            cnt++;
//        }
//
//        assert false; // should never reach this
//        return null;
//    }
//
//
//    /*
//     * Consumes the next token from the tokeniser or the buffer if not empty.
//     */
//    private void nextToken() {
//        if (!buffer.isEmpty())
//            token = buffer.remove();
//        else
//            token = tokeniser.nextToken();
//    }
//
//    /*
//     * If the current token is equals to the expected one, then skip it, otherwise report an error.
//     * Returns the expected token or null if an error occurred.
//     */
//    private Token expect(TokenClass... expected) {
//        for (TokenClass e : expected) {
//            if (e == token.tokenClass) {
//                Token cur = token;
//                nextToken();
//                return cur;
//            }
//        }
//
//        error(expected);
//        return null;
//    }
//
//    /*
//    * Returns true if the current token is equals to any of the expected ones.
//    */
//    private boolean accept(TokenClass... expected) {
//        boolean result = false;
//        for (TokenClass e : expected)
//            result |= (e == token.tokenClass);
//        return result;
//    }
//
//
//    private void parseProgram() {
//        parseIncludes();
//        parseStructDecls();
//        parseVarDecls();
//        parseFunDecls();
//        expect(TokenClass.EOF);
//    }
//
//    // includes are ignored, so does not need to return an AST node
//    private void parseIncludes() {
//        if (accept(TokenClass.INCLUDE)) {
//            nextToken();
//            expect(TokenClass.STRING_LITERAL);
//            parseIncludes();
//        }
//    }
//
//    private void parseStructDecls() {
//        // to be completed ...
//    }
//
//    private void parseVarDecls() {
//        // to be completed ...
//    }
//
//    private void parseFunDecls() {
//        // to be completed ...
//    }
//
//    // to be completed ...
//}
