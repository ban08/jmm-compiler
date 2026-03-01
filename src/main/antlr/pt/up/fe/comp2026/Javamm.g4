grammar Javamm;

@header {
    package pt.up.fe.comp2026;
}

CLASS : 'class' ;
INT : 'int' ;
STATIC : 'static' ;
RETURN : 'return' ;
PACKAGE: 'package';
IMPORT: 'import';
PUBLIC: 'public';

INTEGER : '0' | [1-9][0-9]* ;
ID : [a-zA-Z]+ ;

WS : [ \t\n\r\f]+ -> skip ;

SINGLE_COMMENT: '//' ~[\r\n]*-> skip;
BLOCK_COMMENT : '/*' .*? '*/' -> skip;

program
    : importDecl? packageDecl classNode=classDecl EOF
    ;

importDecl:
    IMPORT ID ';'
;

//package is mandatory
packageDecl
    : PACKAGE path += ID ('.' path +=ID)* ';'
    ;

classDecl
    : CLASS name=ID
        '{'
        methodDecl
        '}'
    ;

varDecl
    : typeNode = type name=ID ';'
    ;

param
    : typeNode = type name=ID
;

type
    : name = INT;

methodDecl locals[boolean isStatic=false]
    : visibility=PUBLIC (STATIC {$isStatic=true;})?
        returnType = type name=ID
        '(' params = param  ')'
        '{' varDecl* stmt* '}'
    ;

stmt
    : var = ID '=' expr ';' #AssignStmt //
    | RETURN expr ';' #ReturnStmt
    ;

expr
    : expr op= '*' expr #BinaryExpr //
    | expr op= '+' expr #BinaryExpr //
    | value=INTEGER #IntegerLiteral //
    | name=ID #VarRefExpr //
    ;



