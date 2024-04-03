package lexer;

import lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;

/**
 * @author cdubach
 */
public class Tokeniser {

    private Scanner scanner;

    private int error = 0;
    public int getErrorCount() {
	return this.error;
    }

    public Tokeniser(Scanner scanner) {
        this.scanner = scanner;
    }

    private void error(char c, int line, int col) {
        System.out.println("Lexing error: unrecognised character ("+c+") at "+line+":"+col);
	error++;
    }


    public Token nextToken() {
        Token result;
        try {
             result = next();
        } catch (EOFException eof) {
            // end of file, nothing to worry about, just return EOF token
            return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // something went horribly wrong, abort
            System.exit(-1);
            return null;
        }
        return result;
    }

    /*
     * To be completed
     */
    private Token next() throws IOException {
        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();
        // skip white spaces
        if (Character.isWhitespace(c))
            return next();
        
        if (c == '/') {
        	c = scanner.peek();
        	if (c != '/' && c != '*') {
        		return new Token(TokenClass.DIV, line, column);
        	}
        	if (c == '*') {
        		boolean end = false;
        		while (!end) {
        			c = scanner.next();
        			if (c == '*') {
        				c = scanner.peek();
        				if (c == '/') {
        					c = scanner.next();
        					end = true;
        				}
        			}
        		}
        		return next();
        	}
        	else {
        		while (c != '\n') {
        			c = scanner.next();
        			c = scanner.peek();
        		}
        	}
        	
        	return next();
        	
        }
        
        if (Character.isLetter(c) || c == '_') {
        	StringBuilder sb = new StringBuilder();
        	sb.append(c);
        	c = scanner.peek();
        	while (Character.isLetterOrDigit(c) || c == '_') {
        		sb.append(c);
        		scanner.next();
        		c = scanner.peek();
        	}
        	
        	String s = sb.toString();
        	if (s.equals("if")) return new Token(TokenClass.IF, line, column); 
        	else if (s.equals("else")) return new Token(TokenClass.ELSE, line, column);
        	else if (s.equals("while")) return new Token(TokenClass.WHILE, line, column);
        	else if (s.equals("return")) return new Token(TokenClass.RETURN, line, column);
        	else if (s.equals("struct")) return new Token(TokenClass.STRUCT, line, column);
        	else if (s.equals("sizeof")) return new Token(TokenClass.SIZEOF, line, column);
        	else if (s.equals("int")) return new Token(TokenClass.INT, line, column); 
        	else if (s.equals("void")) return new Token(TokenClass.VOID, line, column);
        	else if (s.equals("char")) return new Token(TokenClass.CHAR, line, column);
        	else return new Token(TokenClass.IDENTIFIER, s, line, column);
    
        }
        
        if (Character.isDigit(c)) {
        	StringBuilder sb = new StringBuilder();
        	sb.append(c);
        	c = scanner.peek();
        	while (Character.isDigit(c)) {
        		sb.append(c);
        		scanner.next();
        		c = scanner.peek();
        	}
        	return new Token(TokenClass.INT_LITERAL, sb.toString(), line, column);
        }
        
        if (c == '\"') {
        	boolean valid = true;
        	StringBuilder sb = new StringBuilder();
        	c = scanner.peek();
        	while (Character.isDefined(c) && c != '\"') {
        		sb.append(c);
        		c = scanner.next(); 
        		if (c == '\\') {
        			 try {
        				 c = scanner.peek();
        	        } catch (EOFException eof) {
        	        	error(c, line, column);
        	            return new Token(TokenClass.INVALID, line, column);
        	        }
        			if (!(c == 't' || c == 'b' || c == 'n' || c == 'r' || c == 'f' || c == '\'' || c == '\"' || c == '\\' || c == '0')) {valid = false;}
        		}
        		else { 
	        		try {
	        			c = scanner.peek();
	    	        } catch (EOFException eof) {
	    	        	error(c, line, column);
	    	            return new Token(TokenClass.INVALID, line, column);
	    	        }
        		}
        	}
        	if (valid == true) {
        		scanner.next();
            	return new Token(TokenClass.STRING_LITERAL, sb.toString(), line, column);
        	}
        }
        
        if (c == '\'') {
        	String chr = "";
        	try {
            	c = scanner.next();
	        } catch (EOFException eof) {
	        	error(c, line, column);
	            return new Token(TokenClass.INVALID, line, column);
	        }
        	if (c == '\\') {
        		try {
                	c = scanner.next();
    	        } catch (EOFException eof) {
    	        	error(c, line, column);
    	            return new Token(TokenClass.INVALID, line, column);
    	        }
        		if (c == 't' || c == 'b' || c == 'n' || c == 'r' || c == 'f' || c == '\'' || c == '\"' || c == '\\' || c == '0') {
        			switch(c) {
        			case ('t'): chr = "\\t"; break;
        			case ('b'): chr = "\\b"; break;
        			case ('n'): chr = "\\n"; break;
        			case ('r'): chr = "\\r"; break;
        			case ('f'): chr = "\\f"; break;
        			case ('\''): chr = "\\'"; break;
        			case ('\"'): chr = "\\\""; break;
        			case ('\\'): chr = "\\\\"; break;
        			case ('0'): chr = "\\0"; break;
        			}
        			try {
                    	c = scanner.next();
        	        } catch (EOFException eof) {
        	        	error(c, line, column);
        	            return new Token(TokenClass.INVALID, line, column);
        	        }
            		if (c == '\'') {
            			return new Token(TokenClass.CHAR_LITERAL, chr, line, column);
            		}
            	}
//        		else {
//        			error(c, line, column);
//        			return new Token(TokenClass.INVALID, line, column);
//        		}
        	}
        	else if (c != '\'' && c != '\"') {
        		chr = c + "";
        		try {
                	c = scanner.next();
    	        } catch (EOFException eof) {
    	        	error(c, line, column);
    	            return new Token(TokenClass.INVALID, line, column);
    	        }
        		if (c == '\'') {
        			return new Token(TokenClass.CHAR_LITERAL, chr, line, column);
        		}
       		}
        	try {
            	c = scanner.next();
	        } catch (EOFException eof) {
	        	error(c, line, column);
	            return new Token(TokenClass.INVALID, line, column);
	        }
        }
        
        if (c == '#') {
        	StringBuilder sb = new StringBuilder();
        	c = scanner.peek();
        	for (int i = 0; i < 7; i++) {
        		sb.append(c);
        		c = scanner.next();
        		c = scanner.peek();
        	}
        	if (sb.toString().equals("include")) return new Token(TokenClass.INCLUDE, line, column); 
        }

        // recognises the plus operator
        if (c == '+') 
            return new Token(TokenClass.PLUS, line, column); 
        // ... to be completed
        if (c == '-') 
        	return new Token(TokenClass.MINUS, line, column);
        
        if (c == '*') 
        	return new Token(TokenClass.ASTERIX, line, column);
       
        
        if (c == '%') 
        	return new Token(TokenClass.REM, line, column);
        
        if (c == '.') 
        	return new Token(TokenClass.DOT, line, column);
        
        if (c == '&') {
        	try {
            	c = scanner.peek();
	        } catch (EOFException eof) {
	        	error(c, line, column);
	            return new Token(TokenClass.INVALID, line, column);
	        }  	
        	if (c == '&') {
        		scanner.next();
        		return new Token(TokenClass.AND, line, column); 
        	}
        } 
        
        if (c == '|') {
        	try {
            	c = scanner.peek();
	        } catch (EOFException eof) {
	        	error(c, line, column);
	            return new Token(TokenClass.INVALID, line, column);
	        }  	
        	if (c == '|') {
        		scanner.next();
        		return new Token(TokenClass.OR, line, column); 
        	}
        } 
        
        if (c == '=') {
        	try {
            	c = scanner.peek();
	        } catch (EOFException eof) {
	        	error(c, line, column);
	            return new Token(TokenClass.INVALID, line, column);
	        }  	
        	if (c == '=') {
        		scanner.next();
        		return new Token(TokenClass.EQ, line, column); 
        	}
        	else return new Token(TokenClass.ASSIGN, line, column);
        } 
        
        if (c == '!') {
        	try {
            	c = scanner.peek();
	        } catch (EOFException eof) {
	        	error(c, line, column);
	            return new Token(TokenClass.INVALID, line, column);
	        }  	
        	if (c == '=') {
        		scanner.next();
        		return new Token(TokenClass.NE, line, column); 
        	}
        } 
        
        if (c == '<') {
        	try {
            	c = scanner.peek();
	        } catch (EOFException eof) {
	        	error(c, line, column);
	            return new Token(TokenClass.INVALID, line, column);
	        }  	
        	if (c == '=') {
        		scanner.next();
        		return new Token(TokenClass.LE, line, column); 
        	}
        	else return new Token(TokenClass.LT, line, column);
        } 
        
        if (c == '>') {
        	try {
            	c = scanner.peek();
	        } catch (EOFException eof) {
	        	error(c, line, column);
	            return new Token(TokenClass.INVALID, line, column);
	        }  	
        	if (c == '=') {
        		scanner.next();
        		return new Token(TokenClass.GE, line, column); 
        	}
        	else return new Token(TokenClass.GT, line, column);
        } 
        
        if (c == '{') 
        	return new Token(TokenClass.LBRA, line, column);
        
        if (c == '}') 
        	return new Token(TokenClass.RBRA, line, column);
        
        if (c == '(') 
        	return new Token(TokenClass.LPAR, line, column);
        
        if (c == ')') 
        	return new Token(TokenClass.RPAR, line, column);
        
        if (c == '[') 
        	return new Token(TokenClass.LSBR, line, column);
        
        if (c == ']') 
        	return new Token(TokenClass.RSBR, line, column);
        
        if (c == ';') 
        	return new Token(TokenClass.SC, line, column);
        
        if (c == ',') 
        	return new Token(TokenClass.COMMA, line, column);

        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }


}
