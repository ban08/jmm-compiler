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
TRUE : 'true' ;
FALSE : 'false' ;
THIS : 'this' ;
STRING : 'String' ;
FOR : 'for' ;

INTEGER : '0' | [1-9][0-9]* ;
ID : [a-zA-Z_$][a-zA-Z0-9_$]* ;

WS : [ \t\n\r\f]+ -> skip ;

SINGLE_COMMENT: '//' ~[\r\n]*-> skip;
BLOCK_COMMENT : '/*' .*? '*/' -> skip;

program
    : packageDecl importDecl* classNode=classDecl EOF
    ;

importDecl
    : IMPORT path += ID ('.' path += ID)* ';'
    ;

//package is mandatory
packageDecl
    : PACKAGE path += ID ('.' path +=ID)* ';'
    ;

classDecl
    : CLASS name=ID (EXTENDS superName=ID)?
        '{'
        (varDecl | methodDecl)*
        '}'
    ;

varDecl
    : typeNode = type name=ID ('=' expr)? ';'
    ;

param
    : typeNode = type name = ID
    ;

type locals[boolean isArray=false]
    : name = INT ('[' ']' {$isArray=true;})?
    | name = BOOLEAN
    | name = VOID
    | name = STRING ('[' ']' {$isArray=true;})?
    | name = ID ('[' ']' {$isArray=true;})?
    ;

methodDecl locals[boolean isStatic=false]
    : (visibility=(PUBLIC | PRIVATE | PROTECTED))? (STATIC {$isStatic=true;})?
        returnType = type name=ID
        '(' (param (',' param)*)? ')'
        '{' varDecl* stmt* '}'
    ;

stmt
    : '{' stmt* '}' #CompoundStmt
    | IF '(' expr ')' stmt (ELSE stmt)? #IfStmt
    | WHILE '(' expr ')' stmt #WhileStmt
    | FOR '(' (initId=ID '=' initExpr=expr)? ';' (condition=expr)? ';' (stepId=ID '=' stepExpr=expr)? ')' body=stmt #ForStmt
    | expr ';' #ExprStmt
    | var = ID '=' expr ';' #AssignStmt
    | var = ID '[' expr ']' '=' expr ';' #ArrayAssignStmt
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
    | NEW INT '[' expr ']' #NewIntArrayExpr
    | NEW name=(ID | STRING) '(' ')' #NewExpr
    | expr op=('*' | '/' | '%') expr #BinaryExpr
    | expr op=('+' | '-') expr #BinaryExpr
    | expr op=('<' | '>' | '<=' | '>=') expr #BinaryExpr
    | expr op=('==' | '!=') expr #BinaryExpr
    | expr op='&&' expr #BinaryExpr
    | expr op='||' expr #BinaryExpr
    | value=INTEGER #IntegerLiteral
    | value=TRUE #BooleanLiteral
    | value=FALSE #BooleanLiteral
    | name=ID #VarRefExpr
    | THIS #ThisExpr
    ;
