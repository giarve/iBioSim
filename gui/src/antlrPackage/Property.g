grammar Property;


options {
  language = Java;
  output = AST;
  ASTLabelType = CommonTree;

}

@header {
  //package lpn.parser.properties;
  //import lpn.parser.LhpnFile;
  package antlrPackage;
}

@lexer::header {
  //package lpn.parser.properties;
  package antlrPackage;
}



program
	:property
	;
	
property
: 'property'^ ID LCURL! (declaration)* (statement)* RCURL!
;
	
declaration
  :BOOLEAN^ ID (COMMA! ID)* SEMICOL!
  | REAL^ ID (COMMA! ID)* SEMICOL!
  | INT^ ID (COMMA! ID)* SEMICOL!
  ;
  
BOOLEAN
:'boolean'
;

REAL
:'real'
;

INTEGER
:'int'
;


WAIT
:'wait'
;

    
NOT
: '~'
;

MOD
:'%'
;


AND
:'&'
;

OR
:'|'
;


ASSERT
:'assert'
;


IF
:'if'
;


END
:'end'
;


ELSEIF
:'else if'
;

ELSE
:'else'
;

WAIT_STABLE
:'waitStable'
;

ASSERT_UNTIL
:'assertUntil'
;


ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

INT :	'0'..'9'+
    ;

FLOAT
    :   ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
    |   '.' ('0'..'9')+ EXPONENT?
    |   ('0'..'9')+ EXPONENT
    ;

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        |'\r\n'
        ) {$channel=HIDDEN;}
    ;
    


STRING
    :  '\'' ( ESC_SEQ | ~('\\'|'\'') )* '\''
    ;


fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;




PLUS 
:'+'
;

MINUS
: '-'
;


MULT
:'*'
;

DIV
:'/'
;



EQUAL
:'='
;


NOT_EQUAL
:'!='
;

GET
:'>'
;

LET
:'<'
;

GETEQ
:'>='
;
LETEQ
:'<='
;


SAMEAS
:'=='
;



LPARA
:'('
;

RPARA
:')'
;

LCURL
:'{'
;

RCURL
:'}'
;



SEMICOL
:';'
;

COMMA
:','
;



booleanNegationExpression
: (NOT^)* constantValue
;
  

signExpression
:(PLUS^|MINUS^)*  booleanNegationExpression
;
multiplyingExpression
  : signExpression ((MULT^|DIV^|MOD^) signExpression)*
  ;

addingExpression
  : multiplyingExpression ((PLUS^|MINUS^) multiplyingExpression)*
  ;
  

relationalExpression
  : addingExpression ((EQUAL^|NOT_EQUAL^|GET^|GETEQ^|LET^|LETEQ^|SAMEAS^) addingExpression)*
  ;
  
//primitiveElement
//	:constantValue
//	;  

logicalExpression
   : relationalExpression ((AND^|OR^) relationalExpression)*
	 ;
	 
unaryExpression
	: NOT^ LPARA! logicalExpression RPARA! SEMICOL!
  
	;
	 
expression
	//: relational
  //:constantValue
  //|primitiveElement
  //|addingExpression
  //|multiplyingExpression
  : unaryExpression
  |logicalExpression
//  |signExpression
  //|booleanNegationExpression
  ;


constantValue
	: INT | ID
	;

wait_statement
	: WAIT^ LPARA! expression RPARA! SEMICOL!
	| WAIT^ LPARA! expression COMMA!  expression RPARA! SEMICOL!
	;
		
	

assert_statement
	: ASSERT^ LPARA! expression COMMA! expression RPARA! SEMICOL!
	;
	
if_statement
  : IF^ if_part
  ;
  
if_part
 	: LPARA!expression RPARA! LCURL! (statement)* RCURL! (else_if)* (else_part)*
  ;

else_if
	: ELSEIF^  LPARA!expression RPARA!  LCURL! (statement)*  RCURL! 
	;
	
else_part
	:ELSE^  LCURL! (statement)*  RCURL!
	;	
	
waitStable_statement
:WAIT_STABLE^ LPARA! expression COMMA! expression RPARA! SEMICOL!
;
assertUntil_statement
:ASSERT_UNTIL^ LPARA! expression COMMA! expression RPARA! SEMICOL!
;
statement
	: wait_statement
	| assert_statement
	| if_statement
	|waitStable_statement
	|assertUntil_statement
	; 
	
//assignment
//	: variable EQUAL^ expression
//	;
	
//variable
	//: ID
//	;
