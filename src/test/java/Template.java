// libPath=/Users/hdr/Library/ApplicationSupport/JetBrains/Toolbox/apps/IDEA-U/ch-0/203.6682.168/IntelliJ IDEA.app/Contents/lib/
// if you want to use other third party jar
// add the jar directory absolute path to the libPath
// e.g libPath=/../thirdPartyLib/:/../thirdPartyLibTwo/  (!!! end with lineSeparator !!!)

// (!!! not stable !!!)
// if you want to use the Idea's util class .e.g PsiClass
// add your idea lib directory absolute path to the libPath
// e.g for mac /.../IntelliJ IDEA.app/Contents/lib/

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiJavaFile;
import me.lotabout.codegenerator.ext.Optional;
public class Template {

    /** What is inside the {@param context} See {@link me.lotabout.codegenerator.ext.Doc} */
    @SuppressWarnings("unchecked")
    public String build(Map<String, Object> context){

        PsiJavaFile psiJavaFile = (PsiJavaFile) context.get("PsiJavaFile");
        PsiImportList importList = psiJavaFile.getImportList();

        PrettyPrinterConfiguration printConfig = new PrettyPrinterConfiguration();
        StringBuilder sb = new StringBuilder();
        Optional.ofNullable(context.get("methods"))
                .map(obj-> (List<MethodDeclaration>) obj)
                .filter(CollectionUtils::isNotEmpty)
                .stream()
                .flatMap(Collection::stream)
                .forEach(methodDeclarations -> {
                    sb.append(methodDeclarations.toString(printConfig));
                    sb.append(System.lineSeparator());
                });
        return sb.toString();
    }

}

