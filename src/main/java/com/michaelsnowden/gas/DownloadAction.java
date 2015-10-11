package com.michaelsnowden.gas;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;

import javax.swing.*;
import java.io.IOException;

/**
 * @author michael.snowden
 */
public class DownloadAction extends AnAction {
    public void actionPerformed(AnActionEvent actionEvent) {
        try {
            com.intellij.openapi.project.Project project = actionEvent.getProject();
            assert project != null;
            Project gasProject = getGasProject(project);
            final DataContext dataContext = actionEvent.getDataContext();
            final PsiFile currentFile = DataKeys.PSI_FILE.getData(dataContext);
            importGasProject(currentFile, project, gasProject);
            System.out.println("");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private Project getGasProject(com.intellij.openapi.project.Project project) throws IOException {
        final String message = "What is your GAS project ID?";
        final String title = "Input Your GAS Project ID";
        final Icon questionIcon = Messages.getQuestionIcon();
        final String projectId = Messages.showInputDialog(project, message, title, questionIcon);
        return Project.downloadGASProject(DriveFactory.getDriveService(), projectId);
    }

    private void importGasProject(PsiFile currentFile, com.intellij.openapi.project.Project project, Project gasProject) {
        VirtualFile chooseFile = project.getBaseDir();
        if (currentFile != null) {
            chooseFile = currentFile.getVirtualFile();
        }
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        PsiManager psiManager = PsiManager.getInstance(project);
        chooseFile = FileChooser.chooseFile(descriptor, project, chooseFile);
        if (chooseFile == null) {
            return;
        }
        final PsiDirectory directory = psiManager.findDirectory(chooseFile);

        FileType gs = FileTypeManager.getInstance().getFileTypeByExtension("gs");
        for (File gasFile : gasProject.getFiles()) {
            String source = gasFile.getSource();
            String name = gasFile.getName() + ".gs";
            final PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(name, gs, source);

            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    if (directory != null) {
                        directory.add(file);
                    }
                }
            });
        }
    }
}