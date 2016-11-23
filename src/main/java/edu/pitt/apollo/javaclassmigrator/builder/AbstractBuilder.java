package edu.pitt.apollo.javaclassmigrator.builder;

import java.io.FileNotFoundException;

/**
 * Created by nem41 on 11/23/16.
 */
public abstract class AbstractBuilder {
    private final StringBuilder stBuilder;
    protected final String outputDirectory;
    protected final String packageName;
    protected final Class newClass, oldClass;

    public AbstractBuilder(Class newClass, Class oldClass, String outputDirectory, String packageName) {
        stBuilder = new StringBuilder();
        this.newClass = newClass;
        this.oldClass = oldClass;
        this.outputDirectory = outputDirectory;
        this.packageName = packageName;
    }

    public void build() throws FileNotFoundException, ClassNotFoundException {
        buildClassDefinition();
        buildMethods();
        completeClass();
        printSetterFile();
    }

    protected abstract void buildClassDefinition();

    protected abstract void buildMethods() throws ClassNotFoundException, FileNotFoundException;

    protected abstract void completeClass();

    public abstract void printSetterFile() throws FileNotFoundException;

}
