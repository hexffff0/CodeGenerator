package me.lotabout.codegenerator.ui;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ui.Messages;
import me.lotabout.codegenerator.config.PipelineStep;

public class SelectionPane implements PipelineStepConfig {

    public JTextField classNameText;

    public JButton removeButton;

    public JPanel topPanel;

    public JScrollPane contentPane;

    public SelectionPane(PipelineStep config, TemplateEditPane parent) {
        // 监听删除按钮
        removeButton.addActionListener(e -> {
            int result = Messages.showYesNoDialog("Really remove this step?", "Delete", null);
            if (result == Messages.OK) {
                parent.removePipelineStep(this);
            }
        });

        // 添加编辑面板
        EditorFactory factory = EditorFactory.getInstance();
        Document javaTemplate = factory.createDocument("");
        Editor editor = factory.createEditor(javaTemplate, null,
            FileTypeManager.getInstance().getFileTypeByExtension("java"), false);
        contentPane.setViewportView(editor.getComponent());
    }

    @Override
    public PipelineStep getConfig() {
        return null;
    }



    @Override
    public JComponent getComponent() {
        return topPanel;
    }
}
