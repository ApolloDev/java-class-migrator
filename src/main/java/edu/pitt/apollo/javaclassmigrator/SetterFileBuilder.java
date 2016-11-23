/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.apollo.javaclassmigrator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nem41
 */
public class SetterFileBuilder {

	private final String oldClassVariableName;
	private final String newClassVariableName;
	private final StringBuilder stBuilder;
	private final Class newClass, oldClass;
	private final List<String> setters;
	private final String outputDirectory;

	public SetterFileBuilder(Class newClass, Class oldClass, String outputDirectory) {
		stBuilder = new StringBuilder();
		setters = new ArrayList<>();
		this.newClass = newClass;
		this.oldClass = oldClass;
		this.outputDirectory = outputDirectory;

		String variableName = newClass.getSimpleName();
		oldClassVariableName = "old" + variableName;
		newClassVariableName = "new" + variableName;
	}

	public void printSetterFile() throws FileNotFoundException {
		File outputFile = new File(outputDirectory + File.separator + newClass.getSimpleName() + ".java");
		PrintStream ps = new PrintStream(outputFile);
		ps.print(stBuilder.toString());
		ps.close();
	}

	public void build() throws FileNotFoundException {
		buildClassDefinition();
		buildMethods();
		createNewVariableGetter();
		createNewVariableSetter();
		completeClass();
		printSetterFile();
	}

	private void buildClassDefinition() {

		String newClassName = newClass.getCanonicalName();
		String oldClassName = oldClass.getCanonicalName();

		stBuilder.append("public class ").append(newClassName).append("Setter {\n\n");
		stBuilder.append("\tprivate final ").append(oldClassName).append(" ").append(oldClassVariableName).append(";\n");
		stBuilder.append("\tprivate final ").append(newClassName).append(" ").append(newClassVariableName).append(";\n\n");

		stBuilder.append("\tpublic ").append(newClassName).append("Setter(").append(oldClassName)
				.append(" ").append(oldClassVariableName).append(") {\n");
		stBuilder.append("\t\tthis.").append(oldClassVariableName)
				.append(" = ").append(oldClassVariableName).append(";");
		stBuilder.append("\n\t}\n\n");
	}

	private void buildMethods() {
		Method[] allMethods = newClass.getMethods();

		for (int i = 0; i < allMethods.length; i++) {
			Method method = allMethods[i];
			String methodName = method.getName();
			if (methodName.contains("get") && !methodName.contains("Class")) {
				String getMethodName = methodName;
				methodName = methodName.replace("get", "set");
				stBuilder.append("\tprivate void ").append(methodName).append("() {\n");

				setters.add(methodName);

				Class subClass = method.getReturnType();
				if (subClass.getName().toLowerCase().contains("enum")) {
					stBuilder.append("\t\t").append(newClassVariableName).append(".").append(methodName).append("(");
					stBuilder.append(subClass.getCanonicalName()).append(".fromValue(").append(oldClassVariableName).append(".toString()");
				} else if (subClass.isPrimitive()) {
					stBuilder.append("\t\t").append(newClassVariableName).append(".").append(methodName).append("(");
					stBuilder.append(oldClassVariableName).append(".").append(getMethodName).append("()");
				} else {
					//					System.out.println("\t" + method.getReturnType().getName() + " " + method.getName().replaceAll("get", "").toLowerCase() + " = " + method.getName().replaceAll("get", ""));
					stBuilder.append("\t\t").append(subClass.getCanonicalName()).append("Setter setter = new ").append(subClass.getCanonicalName()).append("Setter(");
					stBuilder.append(oldClassVariableName).append(".").append(getMethodName).append("());\n");
					stBuilder.append("\t\tsetter.set();\n");
					stBuilder.append("\t\t").append(newClassVariableName).append(".").append(methodName).append("(");
					stBuilder.append("setter.get").append(subClass.getSimpleName()).append("()");
				}
				stBuilder.append(");\n\t}\n\n");

			}
		}
	}

	private void createNewVariableGetter() {
		stBuilder.append("\tpublic ").append(newClass.getCanonicalName()).append(" get").append(newClass.getSimpleName()).append("() {\n");
		stBuilder.append("\t\treturn ").append(newClassVariableName).append(";\n");
		stBuilder.append("\t}\n\n");
	}

	private void createNewVariableSetter() {

		stBuilder.append("\tpublic void set() {\n");

		for (String setMethod : setters) {
			stBuilder.append("\t\t").append(setMethod).append("();\n");
		}

		stBuilder.append("\t}\n\n");
	}

	private void completeClass() {
		stBuilder.append("}");
	}
}
