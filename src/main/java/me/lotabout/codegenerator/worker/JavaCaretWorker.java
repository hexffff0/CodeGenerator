package me.lotabout.codegenerator.worker;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.util.GenerationUtil;

public class JavaCaretWorker {

    public static void execute(@NotNull CodeTemplate codeTemplate, @NotNull PsiJavaFile file, @NotNull Editor editor, @NotNull Map<String, Object> context) {
        final Project project = file.getProject();

//        String content = GenerationUtil.compileAndExecuteCodeTemplate(codeTemplate, context);

        String content = "";
        //Access document, caret, and selection
        final Document document = editor.getDocument();
        final SelectionModel selectionModel = editor.getSelectionModel();

        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.replaceString(start, end, content);
            PsiDocumentManager.getInstance(project).commitDocument(document);
            PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
            PsiClass clazz = PsiTreeUtil.getParentOfType(element, PsiClass.class, false);
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(clazz.getContainingFile());
        });
        selectionModel.removeSelection();
    }
}
