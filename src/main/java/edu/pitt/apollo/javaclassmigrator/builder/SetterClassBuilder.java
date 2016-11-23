/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.apollo.javaclassmigrator.builder;

import edu.pitt.apollo.javaclassmigrator.util.MigrationUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nem41
 */
public class SetterClassBuilder extends AbstractBuilder {

    private static final String NEW_TYPE_INSTANCE = "newTypeInstance";
    private static final String OLD_TYPE_INSTANCE = "oldTypeInstance";
    private final StringBuilder stBuilder;
    private final List<String> setters;

    public SetterClassBuilder(Class newClass, Class oldClass, String outputDirectory, String packageName) {
        super(newClass, oldClass, outputDirectory, packageName);
        stBuilder = new StringBuilder();
        setters = new ArrayList<>();
    }

    @Override
    public void printSetterFile() throws FileNotFoundException {
        MigrationUtility.writeClassTofile(stBuilder.toString(),
                outputDirectory, newClass.getSimpleName() + "Setter");

//        System.out.println(stBuilder.toString());
    }

    @Override
    protected void buildClassDefinition() {

        String newClassName = newClass.getCanonicalName();
        String oldClassName = oldClass.getCanonicalName();

        stBuilder.append("package ").append(packageName).append(";\n\n");
        stBuilder.append("import edu.pitt.apollo.javaclassmigrator.exception.SetterInitializationException;\n\n");

        stBuilder.append("public class ").append(newClass.getSimpleName()).append("Setter extends ");
        stBuilder.append(MigrationUtility.ABSTRACT_SETTER_CLASS_NAME).append("<");
        stBuilder.append(newClassName).append(",").append(oldClassName).append(">").append(" {\n\n");

        stBuilder.append("\tpublic ").append(newClass.getSimpleName()).append("Setter(")
                .append("Class<").append(newClassName).append("> newTypeClass, ")
                .append(oldClassName)
                .append(" ").append(OLD_TYPE_INSTANCE).append(") throws SetterInitializationException {\n");
        stBuilder.append("\t\tsuper(").append("newTypeClass").append(", ").append(OLD_TYPE_INSTANCE).append(");\n");
        stBuilder.append("\n\t}\n\n");
    }

    @Override
    protected void buildMethods() throws ClassNotFoundException, FileNotFoundException {
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
                    stBuilder.append("\t\t").append(NEW_TYPE_INSTANCE).append(".").append(methodName).append("(");
                    stBuilder.append(subClass.getCanonicalName()).append(".fromValue(").append(OLD_TYPE_INSTANCE).append(".toString())");
                } else if (subClass.isPrimitive()) {
                    stBuilder.append("\t\t").append(NEW_TYPE_INSTANCE).append(".").append(methodName).append("(");
                    stBuilder.append(OLD_TYPE_INSTANCE).append(".").append(getMethodName).append("()");
                } else if (subClass.getSimpleName().equals("BigInteger")) {

                } else {
                    if (MigrationUtility.classHasSubclasses(subClass)) {
                        stBuilder.append("\t\t").append(subClass.getSimpleName()).append("Setter setter = ").append(subClass.getSimpleName()).append("SetterFactory.getSetter();\n");
                    } else {
                        stBuilder.append("\t\t").append(subClass.getSimpleName()).append("Setter setter = new ").append(subClass.getSimpleName()).append("Setter(");
                        stBuilder.append(OLD_TYPE_INSTANCE).append(".").append(getMethodName).append("());\n");
                    }

                    stBuilder.append("\t\tsetter.set();\n");
                    stBuilder.append("\t\t").append(OLD_TYPE_INSTANCE).append(".").append(methodName).append("(");
                    stBuilder.append("setter.").append(MigrationUtility.ABSTRACT_SETTER_GET_NEW_TYPE_METHOD).append("()");

                    if (!subClassSetterExists(subClass)) {
                        // create setter class file
                        AbstractBuilder builder = AbstractBuilderFactory.getBuilder(subClass,
                                getSubClassForOldTypePackage(subClass), outputDirectory, packageName);
                        builder.build();
                    }
                }
                stBuilder.append(");\n\t}\n\n");

            }
        }

        createNewVariableSetter();
    }

    private Class getSubClassForOldTypePackage(Class subClass) throws ClassNotFoundException {
        return Class.forName(oldClass.getPackage().getName() + "." + subClass.getSimpleName());
    }

    private boolean subClassSetterExists(Class subClass) {
        File file = new File(outputDirectory + File.separator + subClass.getSimpleName() + "Setter.java");
        if (!file.exists()) {
            File factoryFile = new File(outputDirectory + File.separator + subClass.getSimpleName() + "SetterFactory.java");
            return factoryFile.exists();
        }

        return true;
    }

    private void createNewVariableSetter() {

        stBuilder.append("\t@Override\n");
        stBuilder.append("\tpublic void set() {\n");

        for (String setMethod : setters) {
            stBuilder.append("\t\t").append(setMethod).append("();\n");
        }

        stBuilder.append("\t}\n\n");
    }

    @Override
    protected void completeClass() {
        stBuilder.append("}");
    }
}
