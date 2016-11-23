package edu.pitt.apollo.javaclassmigrator.builder;

import java.io.FileNotFoundException;

/**
 * Created by nem41 on 11/23/16.
 */
public class SetterClassFactoryBuilder extends AbstractBuilder {


    public SetterClassFactoryBuilder(Class newClass, Class oldClass, String outputDirectory, String packageName) {
        super(newClass, oldClass, outputDirectory, packageName);
    }

    @Override
    protected void buildClassDefinition() {
        System.out.println("Not implemented yet");
    }

    @Override
    protected void buildMethods() throws ClassNotFoundException, FileNotFoundException {

    }

    @Override
    protected void completeClass() {

    }

    @Override
    public void printSetterFile() throws FileNotFoundException {

    }
}
