# jmm-compiler

A compiler for **Java--** (a subset of Java) that takes source code all the way to runnable JVM bytecode.

Group project of four for the Compilers course at FEUP (2025/26).

## What it does

Reads a `.jmm` file and runs it through the full pipeline:

1. **Lexing and parsing** (ANTLR) into an abstract syntax tree.
2. **Semantic analysis** — symbol table construction, type checking, and validation of imports, inheritance, method signatures, array operations and assignments.
3. **OLLIR** intermediate representation, with optional optimizations.
4. **Jasmin backend** that emits `.j` files and assembles them into `.class` files that run on any JVM.

Two optimization paths can be turned on:

- `-o` — AST-level optimizations applied to a fixed point: constant propagation, constant folding (preserving runtime-failing expressions such as division by zero), branch elimination for statically known conditions, and dead-code elimination.
- `-r=<n>` — register allocation on the OLLIR: liveness analysis builds an interference graph (`def ∪ out`) and graph colouring fits local variables into at most `n` JVM registers, reporting the minimum needed when `n` is too small. `-r=0` minimises registers; `-r=-1` keeps the original allocation.

## Stack

Java 21, ANTLR 4, Gradle, Jasmin. Test suite driven by the course's public test fixtures.

## How to run

Requires JDK 21 and Gradle.

```bash
gradle build                 # compile the compiler and the grammar
./jmm path/to/Program.jmm    # compile a Java-- file
./jmm -o -r=4 path/to/Program.jmm   # with optimizations and 4-register allocation
```

The generated `.class` file can then be run with `java`.
