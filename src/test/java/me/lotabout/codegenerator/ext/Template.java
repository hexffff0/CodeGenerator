// libPath=/Users/hdr/Library/ApplicationSupport/JetBrains/Toolbox/apps/IDEA-U/ch-0/203.6682.168/IntelliJ IDEA.app/Contents/lib/
// id you want to use other third party jar
// add the jar directory absolute path to the libPath
// e.g libPath=/../thirdPartyLib/:/../thirdPartyLibTwo/

// if you want to use the Idea's util class .e.g PsiClass
// add your idea lib directory absolute path to the libPath
// e.g for mac /.../IntelliJ IDEA.app/Contents/lib/


package me.lotabout.codegenerator.ext;

import me.lotabout.codegenerator.ext.Doc;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
public class Template {

    @SuppressWarnings("unchecked")
    public String build(Map<String, Object> context){
        return Optional.ofNullable(context.get("methods"))
                .map(obj -> (List<?>) obj)
                .filter(CollectionUtils::isNotEmpty)
                .map(list -> list.get(0))
                .map(obj -> (MethodDeclaration) obj)
                .map(md -> md.toString(new PrettyPrinterConfiguration()))
                .orElse("null");
    }
}