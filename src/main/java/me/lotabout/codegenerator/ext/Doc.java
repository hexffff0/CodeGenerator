package me.lotabout.codegenerator.ext;

/**
 *  context contains object's
 *  -------------------------------
 *
 *  - ClassName: <String>     The name specified by `Target Class Name`
 *  - PackageName: <String>   The package name specified by `Target Class Name`
 *  - class0: <CompilationUnit>    The class that the action is triggered upon
 *  - class1: <CompilationUnit>    The first selected class, where `1` is the postfix
 *                          you specify in pipeline
 *  - fields: List<Statement>   All selected fields
 *  - methods: List<MethodDeclaration> All selected methods
 *  - members: List<MethodDeclaration  Statement> selected fields+methods
 *  - parentMethod: <MethodDeclaration>  The nearest method that surround the current cursor
 *
 *  Other feature
 *  -------------
 *  - Auto import.      If the generated code contains full qualified name, Code Generator will try to
 *                      import the packages automatically and shorten the name.
 *                      For example `java.util.List<>` -> `List<>`
 * @author hdr
 */
public interface Doc {

}
