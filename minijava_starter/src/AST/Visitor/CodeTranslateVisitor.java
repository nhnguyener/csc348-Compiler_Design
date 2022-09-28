package AST.Visitor;

import java.util.HashMap;
import java.util.Iterator;

import AST.*;
import Symtab.*;

public class CodeTranslateVisitor implements Visitor {

	SymbolTable st = null;
	public int errors = 0;

	public void setSymtab(SymbolTable s) { st = s; }
	 
	public SymbolTable getSymtab() { return st; }
	
	public void report_error(int line, String msg) {
		System.out.println(line + ": " + msg);
		++errors;
	}
	
	private HashMap<String, Integer> ctvLabels = new HashMap<String, Integer>();
	private HashMap<String, Integer> offsets = new HashMap<String, Integer>();
	
	private String getLabel(String s) {
		if ( ctvLabels.containsKey(s) ) {
			int c = ctvLabels.get(s);
			ctvLabels.put(s,  c + 1);
			return s + (c + 1);
		} else {
			ctvLabels.put(s, 1);
			return s + 1;
		}
	}
	
	// Display added for toy example language. Not used in regular MiniJava
	public void visit(Display n) {
		n.e.accept(this);
	}

	// MainClass m;
	// ClassDeclList cl;
	public void visit(Program n) {
		System.out.println( ".text" );
		System.out.println( ".globl" + getLabel("asm_main") );
		
		n.m.accept(this);
		
		if ( n.cl != null ) {
			for (int i = 0; i < n.cl.size(); i++) {
				if ( n.cl.get(i) != null) n.cl.get(i).accept(this);
			}
		}
	}

	// Identifier i1,i2;
	// Statement s;
	public void visit(MainClass n) {
		
		n.i1.accept(this);
		st = st.findScope(n.i1.toString());
		n.i2.accept(this);
		st = st.findScope("main");
		System.out.println( "" );
		System.out.println( getLabel( "asm_main" ) + ":" );			//prologue
		System.out.println( "pushq   %rbp" );
		System.out.println( "movq    %rsp,%rbp" );
		
		n.s.accept(this);
		
		System.out.println( "movq    %rbp,%rsp" ); //epilogue
		System.out.println( "popq    %rbp" );
		System.out.println( "ret" );
		st = st.exitScope();
		st = st.exitScope();
		
		System.out.println( ".data" );
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclSimple n) {
		
		st = st.findScope(n.i.toString());				
		if ( n.vl != null ) {
			for (int i = 0; i < n.vl.size(); i++) {
				if ( n.vl.get(i) != null ) n.vl.get(i).accept(this);
			}
		}
		
		if ( n.ml != null ) {
			for (int i = 0; i < n.ml.size(); i++) {
				if ( n.ml.get(i) != null ) n.ml.get(i).accept(this);
			}
		}
		st = st.exitScope();
		
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public void visit(ClassDeclExtends n) {
		
		if ( n.j != null ) n.j.accept(this);
		
		st = st.findScope(n.i.toString());		
		
		if ( n.vl != null ) {
			for (int i = 0; i < n.vl.size(); i++) {
				if ( n.vl.get(i) != null ) n.vl.get(i).accept(this);
			}
		}
		
		if ( n.ml != null ) {
			for (int i = 0; i < n.ml.size(); i++) {
				if ( n.ml.get(i) != null ) n.ml.get(i).accept(this);
			}
		}
		
		st = st.exitScope();
	}

	// Type t;
	// Identifier i;
	public void visit(VarDecl n) {
		n.t.accept(this);
		n.i.accept(this);
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public void visit(MethodDecl n) {
		n.t.accept(this);
		n.i.accept(this);
		for (int i = 0; i < n.fl.size(); i++) {
			n.fl.get(i).accept(this);
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.get(i).accept(this);
		}
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
		n.e.accept(this);
	}

	// Type t;
	// Identifier i;
	public void visit(Formal n) {
		n.t.accept(this);
		n.i.accept(this);
	}

	public void visit(IntArrayType n) {
	}

	public void visit(BooleanType n) {
	}

	public void visit(IntegerType n) {
	}

	// String s;
	public void visit(IdentifierType n) {
	}

	// StatementList sl;
	public void visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.get(i).accept(this);
		}
	}

	// Exp e;
	// Statement s1,s2;
	public void visit(If n) {
		n.e.accept(this);
		n.s1.accept(this);
		n.s2.accept(this);
	}

	// Exp e;
	// Statement s;
	public void visit(While n) {
		n.e.accept(this);
		n.s.accept(this);
	}

	// Exp e;
	public void visit(Print n) {
		n.e.accept(this);
		System.out.println( "movq    %rax,%rdi" );
		System.out.println( "call    put" );
	}

	// Identifier i;
	// Exp e;
	public void visit(Assign n) {
		n.i.accept(this);
		n.e.accept(this);
	}

	// Identifier i;
	// Exp e1,e2;
	public void visit(ArrayAssign n) { //to-do
		n.i.accept(this);
		
		n.e1.accept(this);
		System.out.println( "pushq\t%rax" );
		
		n.e2.accept(this);
		System.out.println( "popq\t%rdx" );
		
		System.out.println( "movq\t%rax,8(%rcx,%rdx,8)" );
	}

	// Exp e1,e2;
	public void visit(And n) {
		n.e1.accept(this);
		System.out.println( "xor\t1,%rax" );
		System.out.println( "cmpq\t$0%rax" );
		
		if ( n.equals(true) )
			System.out.println( "jump\tjne" );
		else
			System.out.println( "jump\tje" );
		
		n.e2.accept(this);
		System.out.println( "xor\t1,%rax" );
		System.out.println( "cmpq\t$0%rax" );
		
		if ( n.equals(true) )
			System.out.println( "jump\tjne" );
		else
			System.out.println( "jump\tje" );
	}

	// Exp e1,e2;
	public void visit(LessThan n) {
		n.e1.accept(this);
		System.out.println( "pushq\t%rax" );
		
		n.e2.accept(this);
		System.out.println( "popq\t%rdx" );
		
		System.out.println( "cmpq\t%rax,%rdx" );
		
		if ( n.equals(true) )
			System.out.println( "jump\tjl" );
		else
			System.out.println( "jump\tjge" );
	}

	// Exp e1,e2;
	public void visit(Plus n) {
		n.e1.accept(this);
		System.out.println( "pushq\t%rax" );
		
		n.e2.accept(this);
		System.out.println( "popq\t%rdx" );
		
		System.out.println( "addq\t%rdx,%rax" );
	}

	// Exp e1,e2;
	public void visit(Minus n) {
		n.e1.accept(this);
		System.out.println( "pushq\t%rax" );
		
		n.e2.accept(this);
		System.out.println( "popq\t%rdx" );
		
		System.out.println( "subq\t%rdx,%rax" );
		System.out.println( "negq\t%rax" );
	}

	// Exp e1,e2;
	public void visit(Times n) {
		n.e1.accept(this);
		System.out.println( "pushq\t%rax" );
		
		n.e2.accept(this);
		System.out.println( "popq\t%rdx" );
		
		System.out.println( "imulq\t%rdx,%rax" );
	}

	// Exp e1,e2;
	public void visit(ArrayLookup n) {
		n.e1.accept(this);
		System.out.println( "pushq\t%rax" );
		
		n.e2.accept(this);
		System.out.println( "popq\t%rdx" );
		
		System.out.println( "movq\t8(%rdx,%rax,8),%rax" );
	}

	// Exp e;
	public void visit(ArrayLength n) {
		n.e.accept(this);
		System.out.println( "movq\t(%rax),%rax" );
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public void visit(Call n) { //to-do
		
		System.out.println( "pushq\t%rdi" );
		System.out.println( "movq\t%rdi,%rcx" );
		
		n.e.accept(this);
		if( !(n.e instanceof This) )
			System.out.println( "movq\t%rax,%rdi" );
		
		//n.i.accept(this);
		for (int i = 0; i < n.el.size(); i++) {
			n.el.get(i).accept(this);
			
			if (n.el.get(i) instanceof This)
				System.out.println( "pushq\t%rcx" );
			else
				System.out.println( "pushq\t%rax" );
		}
		
		//System.out.println( "movq\t(%rdi),%rax" );
		
	}

	// int i;
	public void visit(IntegerLiteral n) {
		System.out.println( "movq\t$" + n.i + ",%rax" );
	}

	public void visit(True n) {
		System.out.println( "movq\t$1,%rax" );
	}

	public void visit(False n) {
		System.out.println( "movq\t$0,%rax" );
	}

	// String s;
	public void visit(IdentifierExp n) { //to-do
		st = st.findScope(n.s);
		st = st.exitScope();
	}

	public void visit(This n) {
		System.out.println( "movq\t%rdi,%rax" );
	}

	// Exp e;
	public void visit(NewArray n) {
		n.e.accept(this);
		System.out.println( "pushq\t%rax" );
		System.out.println( "incq\t%rax" );
		
		System.out.println( "shlq\t$3,%rax" ); //mem for array
		System.out.println( "pushq\t%rdi" );
		System.out.println( "movq\t%rax%rdi" );
		
		System.out.println( "mjmalloc" );
		System.out.println( "popq\t%rdi" );
		
		System.out.println( "popq\t%rdx" ); //array loop
		System.out.println( "movq\t%rdx,(%rax)" );
		System.out.println( "movq\t$8,%rcx" );
		System.out.println( "pushq\t%rax" );
		
		System.out.println( "" ); //fill array
		System.out.println( "" );
		System.out.println( "" );
		System.out.println( "" );
	}

	// Identifier i;
	public void visit(NewObject n) {
		System.out.println( "call\t" + n.i.s );
	}

	// Exp e;
	public void visit(Not n) {
		n.e.accept(this);
		System.out.println( "xor\t$1,%rax" );
	}

	// String s;
	public void visit(Identifier n) {
	}
}