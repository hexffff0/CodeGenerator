package me.lotabout.codegenerator.ext;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
public class JavaParserDemo {

    private static String classCode = "// libPath=/Users/hdr/Library/ApplicationSupport/JetBrains/Toolbox/apps/IDEA-U/ch-0/203.6682.168/IntelliJ IDEA.app/Contents/lib/\n" +
        "// id you want to use other third party jar\n" +
        "// add the jar directory absolute path to the libPath\n" +
        "// e.g libPath=/../thirdPartyLib/:/../thirdPartyLibTwo/\n" +
        "\n" +
        "// if you want to use the Idea's util class .e.g PsiClass\n" +
        "// add your idea lib directory absolute path to the libPath\n" +
        "// e.g for mac /.../IntelliJ IDEA.app/Contents/lib/\n" +
        "\n" +
        "\n" +
        "package me.lotabout.codegenerator.ext;\n" +
        "import java.util.List;\n" +
        "import java.util.Map;\n" +
        "import java.util.Optional;\n" +
        "import org.apache.commons.collections.CollectionUtils;\n" +
        "import com.intellij.psi.PsiMethod;\n" +
        "import me.lotabout.codegenerator.util.MethodEntry;\n" +
        "public class Template {\n" +
        "\n" +
        "    @SuppressWarnings(\"unchecked\")\n" +
        "    public String build(Map<String, Object> context){\n" +
        "        return Optional.ofNullable(context.get(\"methods\"))\n" +
        "                       .map(obj -> (List<MethodEntry>) obj)\n" +
        "                       .filter(CollectionUtils::isNotEmpty)\n" +
        "                       .map(list -> list.get(0))\n" +
        "                       .map(MethodEntry::getRaw)\n" +
        "                       .map(PsiMethod::getText)\n" +
        "                       .orElse(\"\");\n" +
        "    }\n" +
        "\n" +
        "}";

    private static String methodCode = "@SuppressWarnings(\"unchecked\")\n" +
        "public String build(Map<String, Object> context){\n" +
        "    return Optional.ofNullable(context.get(\"methods\"))\n" +
        "    .map(obj -> (List<MethodEntry>) obj)\n" +
        "    .filter(CollectionUtils::isNotEmpty)\n" +
        "    .map(list -> list.get(0))\n" +
        "    .map(MethodEntry::getRaw)\n" +
        "    .map(PsiMethod::getText)\n" +
        "    .orElse(\"\");\n" +
        "    }";

    public static void main(String[] args) {
        CompilationUnit cu = StaticJavaParser.parse(classCode);
        System.err.println(cu.toString(new PrettyPrinterConfiguration()));
        MethodDeclaration md = StaticJavaParser.parseMethodDeclaration(methodCode);
        System.err.println(md.toString());
    }

}
