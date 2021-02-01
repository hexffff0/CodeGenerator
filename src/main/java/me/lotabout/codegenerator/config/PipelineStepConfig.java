package me.lotabout.codegenerator.config;

import me.lotabout.codegenerator.config.PipelineStep;

import javax.swing.*;

public interface PipelineStepConfig {
    PipelineStep getConfig();
    JComponent getComponent();
}
