package ast;

public class ArrayType implements Type {
    public final Type type;
    public final int numberOfElements;

    public ArrayType(Type type, int numberOfElements) {
	    this.type = type;
	    this.numberOfElements = numberOfElements;
    }

     public <T> T accept(ASTVisitor<T> v) {
	return v.visitArrayType(this);
    }
}
