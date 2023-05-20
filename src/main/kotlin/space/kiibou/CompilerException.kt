package space.kiibou

class CompilerException(javaFile: JavaFile) : RuntimeException("Error compiling class ${javaFile.fqn} at ${javaFile.toUri()}")
