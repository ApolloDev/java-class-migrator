/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.apollo.javaclassmigrator;

import edu.pitt.apollo.javaclassmigrator.builder.AbstractBuilder;
import edu.pitt.apollo.javaclassmigrator.builder.AbstractBuilderFactory;
import edu.pitt.apollo.javaclassmigrator.exception.BuilderException;
import edu.pitt.apollo.javaclassmigrator.util.MigrationUtility;

/**
 *
 * @author nem41
 */
public class Migrator {

    private final String javaFileOutputDirectory;
	private final String javaTestFileOutputDirectory;
    private final String javaPackageName;
	
	public Migrator(String javaFileOutputDirectory, String javaTestFileOutputDirectory, String javaPackageName) {
		this.javaFileOutputDirectory = javaFileOutputDirectory;
		this.javaTestFileOutputDirectory = javaTestFileOutputDirectory;
        this.javaPackageName = javaPackageName;
	}
	
	public void createMigrationFilesForClass(Class newClass, Class oldClass) throws BuilderException {
        MigrationUtility.createInitialClassFiles(javaFileOutputDirectory, javaPackageName);
        AbstractBuilder setterClassBuilder = AbstractBuilderFactory.getBuilder(newClass, oldClass.getCanonicalName(), javaFileOutputDirectory, javaPackageName);
		setterClassBuilder.build();
	}

    public void createMigrationFilesForClass(String newClassString, String oldClassString) throws BuilderException {
        try {
            createMigrationFilesForClass(Class.forName(newClassString), Class.forName(oldClassString));
        } catch (ClassNotFoundException ex) {
            throw new BuilderException("Class " + newClassString + " or " + oldClassString + " could not be found");
        }
    }
}
