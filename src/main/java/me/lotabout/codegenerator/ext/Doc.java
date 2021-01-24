package me.lotabout.codegenerator.ext;

/**
 *  Variables Provided (Class Mode)
 *  -------------------------------
 *  Class mode means you want to create new classes(file).
 *
 *  - ClassName: String     The name spcified by `Target Class Name`
 *  - PackageName: String   The package name specified by `Target Class Name`
 *  - class0: ClassEntry    The class that the action is triggered upon
 *
 *  - class1: ClassEntry    The first selected class, where `1` is the postfix
 *                          you specify in pipeline
 *    ...
 *
 *  - MemberEntry (FieldEntry/MethodEntry common properties)
 *      - FieldEntry
 *      - MethodEntry
 *
 *  Variables Provided (Body Mode)
 *  -----------------------
 *  - class0: ClassEntry         The current class
 *  - fields: List<FieldEntry>   All selected fields
 *  - methods: List<MethodEntry> All selected methods
 *  - members: List<MemberEntry> selected fields+methods
 *  - parentMethod: MethodEntry  The nearest method that surround the current cursor
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
