package me.lotabout.codegenerator;

import com.intellij.openapi.components.ApplicationComponent;
import me.lotabout.codegenerator.ext.Doc;
import org.jetbrains.annotations.NotNull;

public class CodeGenerator implements ApplicationComponent {
    public CodeGenerator() {
    }

    @Override
    public void initComponent() {}

    @Override
    public void disposeComponent() {
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "me.lotabout.codegenerator.CodeGenerator";
    }
}
