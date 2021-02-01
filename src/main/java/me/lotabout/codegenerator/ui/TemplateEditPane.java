package me.lotabout.codegenerator.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.uiDesigner.core.GridConstraints;
import me.lotabout.codegenerator.config.ClassSelectionConfig;
import me.lotabout.codegenerator.config.CodeTemplate;
import me.lotabout.codegenerator.config.PipelineStepConfig;

public class TemplateEditPane {

    private JPanel templateEdit;

    private JTextField templateIdText;

    private JTextField templateNameText;

    private JPanel editorPane;

    private JCheckBox templateEnabledCheckBox;

    private JTabbedPane templateTabbedPane;

    private JButton addClassButton;

    private JTextField classNameText;

    private Editor editor;

    private List<SelectionPane> pipeline = new ArrayList<>();

    public TemplateEditPane(CodeTemplate codeTemplate) {

        templateIdText.setText(codeTemplate.getId());
        templateNameText.setText(codeTemplate.name);
        templateEnabledCheckBox.setSelected(codeTemplate.enabled);
        classNameText.setText(codeTemplate.classNameVm);
        //        codeTemplate.pipeline.forEach(this::addMemberSelection);

        // 新增类按钮
        addClassButton.addActionListener(e -> {
            ClassSelectionConfig config = new ClassSelectionConfig();
            SelectionPane pane = new SelectionPane(config, this);
            pipeline.add(pane);
            templateTabbedPane.addTab("Class", pane.getComponent());
        });

        addVmEditor(codeTemplate.template);
    }

    private void addVmEditor(String template) {
        EditorFactory factory = EditorFactory.getInstance();
        Document javaTemplate = factory.createDocument(template);
        editor = factory.createEditor(javaTemplate, null, FileTypeManager.getInstance().getFileTypeByExtension("java"), false);
        GridConstraints constraints = new GridConstraints(0, 0, 1, 1,
            GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
            null, new Dimension(0, 0),
            null, 0, true);
        editorPane.add(editor.getComponent(), constraints);
    }

    public String id() {
        return templateIdText.getText();
    }

    public String name() {
        return templateNameText.getText();
    }

    public boolean enabled() {
        return templateEnabledCheckBox.isSelected();
    }

    public String template() {
        return editor.getDocument().getText();
    }

    public JPanel templateEdit() {
        return templateEdit;
    }

    public String classNameVm() {
        return classNameText.getText();
    }

    @Override
    public String toString() {
        return this.name();
    }

    public CodeTemplate getCodeTemplate() {
        CodeTemplate template = new CodeTemplate(this.id());
        template.name = this.name();
        template.enabled = this.enabled();
        template.template = this.template();
        template.pipeline = pipeline.stream().map(PipelineStepConfig::getConfig).collect(Collectors.toList());
        template.classNameVm = this.classNameVm();
        return template;
    }

    public void removePipelineStep(PipelineStepConfig stepToRemove) {
        int index = this.pipeline.indexOf(stepToRemove);
        PipelineStepConfig step = this.pipeline.remove(index);
        this.templateTabbedPane.remove(step.getComponent());
    }
}
