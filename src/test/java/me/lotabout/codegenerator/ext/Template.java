// libPath=/Users/hdr/Library/ApplicationSupport/JetBrains/Toolbox/apps/IDEA-U/ch-0/203.6682.168/IntelliJ IDEA.app/Contents/lib/
// id you want to use other third party jar
// add the jar directory absolute path to the libPath
// e.g libPath=/../thirdPartyLib/:/../thirdPartyLibTwo/

// if you want to use the Idea's util class .e.g PsiClass
// add your idea lib directory absolute path to the libPath
// e.g for mac /.../IntelliJ IDEA.app/Contents/lib/


package me.lotabout.codegenerator.ext;

import java.util.Map;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import me.lotabout.codegenerator.ext.Doc;
public class Template {

    @SuppressWarnings("unchecked")
    public String build(Map<String, Object> context){

        CompilationUnit cu = (CompilationUnit) context.get("class1");
        TypeDeclaration<?> td = cu.getTypes().get(0);
        String className = td.getName().asString();

        MethodDeclaration md = new MethodDeclaration();
        md.setModifier(Keyword.PUBLIC, true)
          .setType(className)
          .setName(className)
          .addParameter(className, className)
          .setBody(new BlockStmt().addStatement("return null;"));
        return md.toString(new PrettyPrinterConfiguration());
    }

    public static void main(String[] args) {
        CompilationUnit err = StaticJavaParser.parse("illegal text");
        System.err.println(err);
    }

}