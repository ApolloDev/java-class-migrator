/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.apollo.javaclassmigrator;

import java.io.FileNotFoundException;

/**
 *
 * @author nem41
 */
public class Migrator {
	
	private final String javaFileOutputDirectory;
	private final String javaTestFileOutputDirectory;
	
	public Migrator(String javaFileOutputDirectory, String javaTestFileOutputDirectory) {
		this.javaFileOutputDirectory = javaFileOutputDirectory;
		this.javaTestFileOutputDirectory = javaTestFileOutputDirectory;
	}
	
	public void createMigrationFilesForClass(Class newClazz, Class oldClazz) throws FileNotFoundException {
		SetterFileBuilder setterFileBuilder = new SetterFileBuilder(newClazz, oldClazz, javaFileOutputDirectory);
		setterFileBuilder.build();
	}
	
}
