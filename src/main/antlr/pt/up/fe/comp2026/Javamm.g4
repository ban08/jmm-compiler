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
INTEGER : '0' | [1-9][0-9]* ;
ID : [a-zA-Z_$][a-zA-Z0-9_$]* ;

WS : [ \t\n\r\f]+ -> skip ;

SINGLE_COMMENT: '//' ~[\r\n]*-> skip;
BLOCK_COMMENT : '/*' .*? '*/' -> skip;

program
    : packageDecl importDecl* classNode=classDecl EOF
    ;

importDecl
    : IMPORT path += ID '.' path += ID ('.' path += ID)* ';'
    ;

packageDecl
    : PACKAGE path += ID ('.' path +=ID)* ';'
    ;

classDecl
    : CLASS name=ID (EXTENDS superName=ID)?
        '{'
        (fieldDecl | methodDecl)*
        '}'
    ;

fieldDecl
    : typeNode = type name=ID ('=' expr)? ';'
    ;

varDecl
    : typeNode = type name=ID ';'
    ;

param
    : typeNode = type name = ID
    ;

type
    : elementType=type '[' ']' #ArrayType
    | name = INT #SimpleType
    | name = BOOLEAN #SimpleType
    | name = VOID #SimpleType
    | name = ID #SimpleType
    ;

methodDecl locals[boolean isStatic=false]
    : (visibility=(PUBLIC | PRIVATE | PROTECTED))? (STATIC {$isStatic=true;})?
        returnType = type name=ID
        '(' (param (',' param)*)? ')'
        '{' varDecl* stmt* '}'
    ;

forHeaderAssign
    : var = ID '=' expr
    | var = ID ('[' expr ']')+ '=' expr
    ;

stmt
    : '{' stmt* '}' #CompoundStmt
    | IF '(' expr ')' stmt (ELSE stmt)? #IfStmt
    | WHILE '(' expr ')' stmt #WhileStmt
    | DO stmt WHILE '(' expr ')' ';' #DoWhileStmt
    | FOR '(' forHeaderAssign? ';' expr? ';' forHeaderAssign? ')' stmt #ForStmt
    | expr ';' #ExprStmt
    | var = ID '=' expr ';' #AssignStmt
    | var = ID ('[' expr ']')+ '=' expr ';' #ArrayAssignStmt
    | RETURN expr? ';' #ReturnStmt
    ;

expr
    : '(' expr ')' #ParenExpr
    | expr '.' 'length' #LengthExpr
    | expr '.' name=ID '(' (expr (',' expr)*)? ')' #MethodCallExpr
    | expr '.' name=ID #FieldAccessExpr
    | expr '[' expr ']' #ArrayAccessExpr
    | '!' expr #NotExpr
    | op=('++' | '--' | '+' | '-') expr #UnaryExpr
    | NEW INT ('[' expr ']')+ #NewIntArrayExpr
    | NEW INT '[' ']' '{' (expr (',' expr)*)? '}' #ArrayInitializerExpr
    | NEW name=ID '(' (expr (',' expr)*)? ')' #NewExpr
    | expr op=('*' | '/' | '%') expr #BinaryExpr
    | expr op=('+' | '-') expr #BinaryExpr
    | expr op=('<' | '>' | '<=' | '>=') expr #BinaryExpr
    | expr op=('==' | '!=') expr #BinaryExpr
    | expr op='&&' expr #BinaryExpr
    | expr op='||' expr #BinaryExpr
    | name=ID '(' (expr (',' expr)*)? ')' #ImplicitThisCallExpr
    | value=INTEGER #IntegerLiteral
    | value=TRUE #BooleanLiteral
    | value=FALSE #BooleanLiteral
    | name=ID #VarRefExpr
    | THIS #ThisExpr
    ;
