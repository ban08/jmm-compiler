# jmm-compiler

A compiler for **Java--** (a subset of Java) that takes source code all the way to runnable JVM bytecode.

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

## What I built

Group project of four for the Compilers course (2025/26). My part was the middle and back of the pipeline:

- Most of the **semantic analysis**: symbol table population, type checking, and resolution of imports, inherited members and method signatures, including the edge cases around static calls, qualified names and array types.
- The **OLLIR optimizations**: constant propagation and folding, dead-code elimination, and branch elimination.
- **Register allocation** by liveness analysis and graph colouring.
- Core of the **Jasmin backend**: method emission, instruction selection, and keyword escaping.

Teammates worked mainly on array code generation and parts of the frontend grammar.

## What I would do differently

Split the semantic analysis into clearly separated passes from the start instead of growing it into one large stage that later had to be broken apart. I would also add end-to-end tests that run the emitted bytecode and check its output, rather than relying mostly on stage-by-stage fixtures.
