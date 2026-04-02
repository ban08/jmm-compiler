grammar Javamm;

@header {
    package pt.up.fe.comp2026;
}

// Keywords
CLASS : 'class' ;
INT : 'int' ;
BOOLEAN : 'boolean' ;
VOID : 'void' ;
STATIC : 'static' ;
RETURN : 'return' ;
PACKAGE: 'package';
IMPORT: 'import';
PUBLIC: 'public';
PRIVATE: 'private';
PROTECTED: 'protected';
EXTENDS: 'extends' ;
NEW : 'new' ;
IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;
DO : 'do' ;
FOR : 'for' ;
TRUE : 'true' ;
FALSE : 'false' ;
THIS : 'this' ;
LENGTH : 'length' ;
INTEGER : '0' | [1-9][0-9]* ;
ID : [a-zA-Z_$][a-zA-Z0-9_$]* ;

WS : [ \t\n\r\f]+ -> skip ;

SINGLE_COMMENT: '//' ~[\r\n]*-> skip;
BLOCK_COMMENT : '/*' .*? '*/' -> skip;

program
    : packageDecl importDecl* classNode=classDecl EOF
    ;

importDecl
    : IMPORT path += (ID | LENGTH) '.' path += (ID | LENGTH) ('.' path += (ID | LENGTH))* ';'
    ;

packageDecl
    : PACKAGE path += (ID | LENGTH) ('.' path += (ID | LENGTH))* ';'
    ;

classDecl
    : CLASS name=(ID | LENGTH) (EXTENDS superName=(ID | LENGTH))?
        '{'
        (fieldDecl | methodDecl)*
        '}'
    ;

fieldDecl
    : typeNode = type name=(ID | LENGTH) ('=' expr)? ';'
    ;

varDecl
    : typeNode = type name=(ID | LENGTH) ';'
    ;

param
    : typeNode = type name = (ID | LENGTH)
    ;

arrayCreationDim
    : '[' expr? ']'
    ;

type
    : elementType=type '[' ']' #ArrayType
    | name = INT #SimpleType
    | name = BOOLEAN #SimpleType
    | name = VOID #SimpleType
    | name = (ID | LENGTH) #SimpleType
    ;

methodDecl locals[boolean isStatic=false]
    : (visibility=(PUBLIC | PRIVATE | PROTECTED))? (STATIC {$isStatic=true;})?
        returnType = type name=(ID | LENGTH)
        '(' (param (',' param)*)? ')'
        '{' varDecl* stmt* '}'
    ;

forHeaderAssign
    : var = (ID | LENGTH) '=' expr
    | var = (ID | LENGTH) ('[' expr ']')+ '=' expr
    ;

stmt
    : '{' stmt* '}' #CompoundStmt
    | IF '(' expr ')' stmt (ELSE stmt)? #IfStmt
    | WHILE '(' expr ')' stmt #WhileStmt
    | DO stmt WHILE '(' expr ')' ';' #DoWhileStmt
    | FOR '(' forHeaderAssign? ';' expr? ';' forHeaderAssign? ')' stmt #ForStmt
    | expr ';' #ExprStmt
    | var = (ID | LENGTH) '=' expr ';' #AssignStmt
    | var = (ID | LENGTH) ('[' expr ']')+ '=' expr ';' #ArrayAssignStmt
    | RETURN expr? ';' #ReturnStmt
    ;

expr
    : '(' expr ')' #ParenExpr
    | expr '.' name=(ID | LENGTH) '(' (expr (',' expr)*)? ')' #MethodCallExpr
    | expr '.' name=(ID | LENGTH) #FieldAccessExpr
    | expr '[' expr ']' #ArrayAccessExpr
    | '!' expr #NotExpr
    | op=('++' | '--' | '+' | '-') expr #UnaryExpr
    | NEW INT arrayCreationDim+ #NewIntArrayExpr
    | NEW INT '[' ']' '{' (expr (',' expr)*)? '}' #ArrayInitializerExpr
    | NEW name=(ID | LENGTH) '(' (expr (',' expr)*)? ')' #NewExpr
    | expr op=('*' | '/' | '%') expr #BinaryExpr
    | expr op=('+' | '-') expr #BinaryExpr
    | expr op=('<' | '>' | '<=' | '>=') expr #BinaryExpr
    | expr op=('==' | '!=') expr #BinaryExpr
    | expr op='&&' expr #BinaryExpr
    | expr op='||' expr #BinaryExpr
    | name=(ID | LENGTH) '(' (expr (',' expr)*)? ')' #ImplicitThisCallExpr
    | value=INTEGER #IntegerLiteral
    | value=TRUE #BooleanLiteral
    | value=FALSE #BooleanLiteral
    | name=(ID | LENGTH) #VarRefExpr
    | THIS #ThisExpr
    ;
