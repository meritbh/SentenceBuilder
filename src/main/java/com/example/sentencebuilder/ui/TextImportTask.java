package com.example.sentencebuilder.ui;

import com.example.sentencebuilder.logic.ApplicationController;
import com.example.sentencebuilder.logic.ImportSummary;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Runs a text-file import away from the JavaFX application thread.
 */
public class TextImportTask extends Task<ImportSummary> {
    private final Path sourceFile;
    private final ApplicationController applicationController;

    /**
     * Creates a background task for one validated text file.
     *
     * @param sourceFile text file selected by the user
     * @param applicationController shared business-logic controller
     */
    public TextImportTask(Path sourceFile, ApplicationController applicationController) {
        this.sourceFile = Objects.requireNonNull(sourceFile, "sourceFile");
        this.applicationController = Objects.requireNonNull(
                applicationController,
                "applicationController");
    }

    /**
     * Imports the selected file through the business-logic controller and
     * publishes progress/status updates to JavaFX.
     *
     * @return summary of the completed file import
     * @throws Exception if the selected file cannot be read or stored
     */
    @Override
    protected ImportSummary call() throws Exception {
        updateMessage("Preparing " + sourceFile.getFileName() + "...");

        return applicationController.importFile(
                sourceFile,
                this::isCancelled,
                (completedLines, totalLines) -> {
                    if (totalLines == 0) {
                        updateProgress(1, 1);
                    } else {
                        updateProgress(completedLines, totalLines);
                    }

                    updateMessage(String.format(
                            "Processing %s: %,d of %,d lines",
                            sourceFile.getFileName(),
                            completedLines,
                            totalLines));
                });
    }
}
