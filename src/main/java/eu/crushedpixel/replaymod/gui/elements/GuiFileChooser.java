package eu.crushedpixel.replaymod.gui.elements;

import eu.crushedpixel.replaymod.gui.elements.listeners.FileChooseListener;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class GuiFileChooser extends GuiAdvancedButton {

    @Getter
    private File selectedFile;

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
        updateDisplayString();
    }

    protected String baseString;
    private boolean save = false;

    @Setter
    private String[] allowedExtensions = null;

    private List<FileChooseListener> listeners = new ArrayList<FileChooseListener>();

    public GuiFileChooser(int buttonId, int x, int y, String buttonText, File selectedFile, String[] allowedExtensions) {
        this(buttonId, x, y, buttonText, selectedFile, allowedExtensions, false);
    }

    public GuiFileChooser(int buttonId, int x, int y, String buttonText, File selectedFile, String[] allowedExtensions, boolean save) {
        super(buttonId, x, y, buttonText);
        this.selectedFile = selectedFile;
        this.save = save;

        this.baseString = buttonText;
        updateDisplayString();

        this.allowedExtensions = allowedExtensions;
    }

    public void addFileChooseListener(FileChooseListener listener) {
        this.listeners.add(listener);
    }

    protected void updateDisplayString() {
        this.displayString = baseString + (selectedFile == null ? "-" : selectedFile.getName());
    }

    @Override
    public void performAction() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Frame frame = new Frame();
                FileDialog fileDialog = new FileDialog(frame);

                fileDialog.setMode(save ? FileDialog.SAVE : FileDialog.LOAD);

                if(selectedFile != null) {
                    fileDialog.setDirectory(selectedFile.getParentFile().getAbsolutePath());
                    fileDialog.setFile(selectedFile.getName());
                }

                fileDialog.setFilenameFilter(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if(allowedExtensions == null) return true;
                        for(String extension : allowedExtensions) {
                            String[] split = name.split("\\.");
                            String ext = split[split.length - 1];
                            if(extension.equalsIgnoreCase(ext)) return true;
                        }

                        return false;
                    }
                });


                fileDialog.setVisible(true);

                String filename = fileDialog.getFile();
                if(filename != null && FilenameUtils.getExtension(filename).isEmpty() && allowedExtensions.length > 0) {
                    filename += "."+allowedExtensions[0];
                }

                String directory = fileDialog.getDirectory();
                if(filename != null) {
                    selectedFile = new File(directory, filename);

                    updateDisplayString();

                    for(FileChooseListener listener : listeners) {
                        listener.onFileChosen(selectedFile);
                    }
                }

                frame.dispose();
            }
        }, "replaymod-file-chooser").start();
    }
}