
import java.util.ArrayList;




/* *** This file is given as part of the programming assignment. *** */

public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private ArrayList<ArrayList<String>> symTable = new ArrayList<>();
    private int curBlock = 0;
    private boolean first = true;
    private void scan() {
    tok = scanner.scan();
    //System.out.println(tok);
    }

    private Scan scanner;
    Parser(Scan scanner) {
        System.out.println("#include <stdio.h>");
        System.out.println("int main(){");
        if(first){
            addBlock();
            first = false;
            curBlock--;
        }
	this.scanner = scanner;
	scan();
 	program();

	if( tok.kind != TK.EOF )
		//System.out.println(tok+"#############");
	    parse_error("junk after logical end of program");
             System.out.println();
    }

    private void program() {
        
	           block();
                   System.out.println("return 0;");
                   System.out.println("}");
    //System.out.println("end of the program @@@@@@@@@@@@@@@@@@@@@@");
    }

    private void block(){
     
	       declaration_list();
	       statement_list();
    
    }

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
	
	//System.out.println(tok+"@@@@@@@@@@@@@@@@@");

	
	

	while( is(TK.DECLARE) ) {

		//System.out.println("here you go !!!!!!!!");
	    declaration();

	}

	
     
    }//end of declaration_list

    private void ref_id(){
    	if(tok.kind == TK.TILDE){
    		scan();
    		if(tok.kind == TK.NUM){
                    int num = Integer.parseInt(tok.string);                     
                    scan();
                    if(num > curBlock){
                        System.err.println("no such variable ~"+ num +
                                tok.string + " on line "+ tok.lineNumber);
                        System.exit(1);
                    }  
                    if(searchTildaSpec(tok.string,num))
                        System.out.print("x_"+ (curBlock - num) + tok.string);
                }
                else{
                    if(searchTildaGlob(tok.string))
                        System.out.print("x_0"+ tok.string);
                }
                mustbe(TK.ID);
    		
    		

    	}
        else{
                if(searchUndeclared(tok.string)){
                    int numPrint = searchNum(tok.string);
                    System.out.print("x_"+ numPrint + tok.string);
                }
    		mustbe(TK.ID);

    		
    	}
    }

    private void pr(){
    //	System.out.println("I AM IN !!!!!");
    	scan();
    	//mustbe(TK.PRINT);
    	//System.out.println(tok+"*************");
    	// while(tok.kind == TK.NUM){
    	// 	scan();
    	// }
        System.out.print("printf(\"%d\\n\",");
    	expr();
        System.out.println(");");

    }
    private void assign(){
    	ref_id();
        System.out.print("=");
    	mustbe(TK.ASSIGN);
    	expr();
        System.out.println(";");
    }

    private void expr(){
    	   term();
    	while(is(TK.PLUS) || is(TK.MINUS)){
            //System.out.println("I am in here");
            if(is(TK.PLUS))
                System.out.print("+");
            else{
                System.out.print("-");
            }
    		scan();
    		term();
    	}
    }

    private void term(){
        //System.out.println("In the term");
    	factor();
    	while(is(TK.DIVIDE) || is(TK.TIMES)){
            if(is(TK.TIMES)){
                System.out.print("*");
            }
            else{
                System.out.print("/");
            }
    		scan();
    		factor();
    	}

    }
    private void factor(){
    	if (tok.kind == TK.LPAREN){
        System.out.print("(");
    	scan();
    	expr();
        System.out.print(")");
    	mustbe(TK.RPAREN);
    	} else if(tok.kind == TK.NUM){
    		while( is(TK.NUM)){
                         System.out.print(tok.string);
    			//System.out.println(tok+"In the Factor");
    			mustbe(TK.NUM);
    		}
    	}else{
    		ref_id();
    	} 
    }
    private void declaration() {
	mustbe(TK.DECLARE);
        if(searchCurBlock(tok.string))
            System.out.println("int x_"+ curBlock + tok.string + ";");
	mustbe(TK.ID);
	while( is(TK.COMMA) ) {
	    scan();
            if(searchCurBlock(tok.string))
                System.out.println("int x_"+ curBlock + tok.string + ";");
	    mustbe(TK.ID);
	}
    }

    private void statement_list() {

     while(tok.kind == TK.ID || tok.kind == TK.PRINT || tok.kind == TK.DO || tok.kind == TK.IF || tok.kind == TK.TILDE|| tok.kind == TK.FORL  ){


        while( tok.kind == TK.ID || tok.kind == TK.TILDE){
        //System.out.println(tok + "I am in ID");
        assign();
        //System.out.println(tok + "I am done in here");
        }
    
        while( is(TK.PRINT)){

        pr();

        }

    	while( is(TK.IF)){
    		If_statement();
        }

        while( is(TK.DO)){
            Do_statement();
        }

        while(is(TK.FORL)){
            forl_statement();
        }

        while( is(TK.TILDE)){
            ref_id();
            if(is(TK.ASSIGN)){
               System.out.print("=");
               mustbe(TK.ASSIGN);
               expr();
               System.out.println(";");
            }
        }
    }//big while loop
        //System.out.println("out of statement_list");
    }

    private void Do_statement(){
        System.out.print("while");
        mustbe(TK.DO);
        guarded_command();
        mustbe(TK.ENDDO);
    }

    private void forl_statement(){
        System.out.print("for (");
        scan();

        System.out.print("int i = 0; i<=" + tok.string + "; i ++)");
        addBlock();
        System.out.print("{");

        scan();
        block();

        mustbe(TK.ENDFORL);

        System.out.println("}");
        removeBlock();
    }

    private void If_statement(){
        System.out.print("if");
    	mustbe(TK.IF);
    	guarded_command();
    	while( is(TK.ELSEIF)){
                System.out.print("else if");
    		scan();
    		guarded_command();
    	}
    	if(tok.kind == TK.ELSE){
                System.out.print("else");
    		scan();
                addBlock();
                System.out.println("{");
    		block();
                System.out.println("}");
                removeBlock();
    	}
    	mustbe(TK.ENDIF);

    }
    private void guarded_command(){
        System.out.print("(");
    	expr();
        System.out.print(" <= 0");
    	mustbe(TK.THEN);
        System.out.print(")");
        addBlock();
        System.out.println("{");
    	block();
        System.out.println("}");
        removeBlock();
    }

    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
    	
	if( tok.kind != tk ) {
	//	System.out.println(tk+"%%%%%%%%%%%%%%%%%");
	    System.err.println( "mustbe: want " + tk + ", got " +
				    tok);
	    parse_error( "missing token (mustbe)" );
	}
	scan();
    }

    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	//System.out.println(tok+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

	System.exit(1);
    }
   
    private void addBlock(){
        curBlock++;
        ArrayList<String> newBlock = new ArrayList<>();
        symTable.add(newBlock);    
    }
    private void removeBlock(){
        symTable.remove(curBlock);
        curBlock--;
    }
    private boolean searchCurBlock(String curString){
        for(int i = 0; i < symTable.get(curBlock).size(); i++){
            if(curString.equals(symTable.get(curBlock).get(i))){
                System.err.println("redeclaration of variable " + curString);
                return false;
                
            }
            
        }
        symTable.get(curBlock).add(new String(curString));
        return true;
    }
   
    private boolean searchUndeclared(String curString){
        for(int i = symTable.size()-1; i >= 0;i-- ){
            for(int j = 0 ; j < symTable.get(i).size() ; j++){
                 if(curString.equals(symTable.get(i).get(j))){
                    return true;
                 }
            }
        }
        System.err.println(curString + " is an undeclared variable on line " + tok.lineNumber);
        System.exit(1);
        return false;
    }
        private int searchNum(String curString){
        for(int i = symTable.size()-1; i >= 0;i-- ){
            for(int j = 0 ; j < symTable.get(i).size() ; j++){
                 if(curString.equals(symTable.get(i).get(j))){
                    return i;
                 }
            }
        }
        System.err.println(curString + " is an undeclared variable on line " + tok.lineNumber);
        System.exit(1);
        return -1;
    }
    private boolean searchTildaSpec(String curString, int displacement){
       int block = curBlock-displacement;
       for(int i = 0; i < symTable.get(block).size(); i++){
            if(curString.equals(symTable.get(
                    block).get(i))){
                return true;
            }
        }
    System.err.println("no such variable ~"+ displacement + curString + " on line "+ tok.lineNumber);
    System.exit(1);
    return false;
    }
    
    private boolean searchTildaGlob(String curString){
     for(int i = 0; i < symTable.get(0).size(); i++){
            if(curString.equals(symTable.get(0).get(i))){
                return true;
            }
        }
    System.err.println("no such variable ~"+ curString + " on line "+ tok.lineNumber);
    System.exit(1);
    return false;
    }
    
}
