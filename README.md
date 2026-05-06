# Compiler Project

For the last delivery (CP3), complete the following sections.

## Participation

The sum of the participations should be 100%, for a group of four elements with balanced participation, this corresponds to 25% participation each.

[Student 1] - [percentage]% 
[Student 2] - [percentage]% 
[Student 3] - [percentage]% 
[Student 4] - [percentage]% 

## Declaration of AI Tools Used

Please choose one of the two options  about AI tools use. In case tools where used, enumerate which ones, and the specific use.

Finally, check the box regarding responsibility for the work.

AI tools/services used in this work:

[] No AI tools were used.
[] The following tools were used:
 - [Name]: [specific use]
 - [Name]: [specific use]

[] All content has been reviewed, understood, validated, and we assume full responsibility for the work in this repository.


## Implemented CP2 Optimizations

The `-o` flag enables AST-level optimizations that are repeatedly applied until a fixed point:

- Constant propagation for safe local constant values.
- Constant folding for compile-time arithmetic and boolean expressions, preserving runtime-failing expressions such as division by zero.
- Branch elimination for `if`/`else` statements with statically known conditions.
- Dead code elimination for unreachable statements, pure discarded expressions, and dead local stores whose right-hand side can be safely removed.

The `-r=<n>` option enables OLLIR-level register allocation:

- `-r=-1` keeps the original OLLIR variable table allocation.
- `-r=0` minimizes the number of local JVM registers.
- `-r>=1` applies liveness analysis, builds an interference graph from `def U out`, colors only local variables/temporaries, preserves `this` and parameter registers, and reports an error with the required minimum if the requested limit cannot be satisfied.

# Repository Structure

The base repository has several folders, the main ones are:

- `src`: The source folder for the project, you will work here.
- `test`: Folder for your own tests.
- `test-public`: Public tests, similar to the majority of the private tests that will be used for evaluation. **Do not change the contents of this folder.** This folder will be modified by automatic updates during the semester.


The remaining folders are:

- `libs`: Libraries in JAR format, required for the project.
- `libs-jmm`: Java code that can be imported in your Java-- classes. Contains a `java` folder, with the source code, and a `compiled` folder with the same classes, in compiled format. The build system automatically compiles the files inside the `java` folder and stores them in the `compiled` folder.
