/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.apollo.javaclassmigrator.builder;

import edu.pitt.apollo.javaclassmigrator.util.MigrationUtility;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author nem41
 */
public class SetterClassBuilder extends AbstractBuilder {

    protected static final String NEW_TYPE_INSTANCE = "newTypeInstance";
    protected static final String OLD_TYPE_INSTANCE = "oldTypeInstance";
    protected String extendedSetterClassName;
    protected final List<String> setters;

    public SetterClassBuilder(Class newClass, String oldClassName, String outputDirectory, String packageName, Set<String> callSet) {
        super(newClass, oldClassName, outputDirectory, packageName, callSet);
        setters = new ArrayList<>();
        extendedSetterClassName = ABSTRACT_SETTER_CLASS_NAME;
    }

    @Override
    protected String getClassNameForCallSet() {
        return newClass.getCanonicalName();
    }

    @Override
    public void printSetterFile() throws FileNotFoundException {
        writeClassTofile(stBuilder.toString(),
                outputDirectory, newClass.getSimpleName() + "Setter");
    }

    @Override
    protected void buildClassDefinition() {

        String newClassName = newClass.getCanonicalName();

        stBuilder.append("package ").append(packageName).append(";\n\n");

        stBuilder.append("public class ").append(newClass.getSimpleName()).append("Setter extends ");
        stBuilder.append(extendedSetterClassName).append("<");
        stBuilder.append(newClassName).append(",").append(oldClassName).append(">").append(" {\n\n");

        stBuilder.append("\tpublic ").append(newClass.getSimpleName()).append("Setter(")
                .append("Class<").append(newClassName).append("> newTypeClass, ")
                .append(oldClassName)
                .append(" ").append(OLD_TYPE_INSTANCE).append(") throws MigrationException {\n");
        stBuilder.append("\t\tsuper(").append("newTypeClass").append(", ").append(OLD_TYPE_INSTANCE).append(");\n");
        stBuilder.append("\n\t}\n\n");
    }

    @Override
    protected void buildMethods() throws FileNotFoundException {
        Field[] allFields = newClass.getDeclaredFields();

        for (int i = 0; i < allFields.length; i++) {
            Field field = allFields[i];
            String fieldName = field.getName();
            String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            if (!getMethodName.contains("Class")) {

                String methodName = getMethodName.replaceFirst("get", "set");
                Method method;
                try {
                    method = newClass.getMethod(getMethodName);
                } catch (NoSuchMethodException ex) {
                    // try "is" method
                    try {
                        getMethodName = getMethodName.replaceFirst("get", "is");
                        method = newClass.getMethod(getMethodName);
                    } catch (NoSuchMethodException ex1) {
                        warnNoSetterMethodCanBeCreated(newClass, getMethodName);
                        continue;
                    }
                }

                stBuilder.append("\tprivate void ").append(methodName).append("() throws MigrationException {\n");

                setters.add(methodName);

                Class subClass = method.getReturnType();
                if (subClass.getName().toLowerCase().contains("enum")) {
                    stBuilder.append("\t\t").append(NEW_TYPE_INSTANCE).append(".").append(methodName).append("(");
                    stBuilder.append(subClass.getCanonicalName()).append(".fromValue(").append(OLD_TYPE_INSTANCE).append(".").append(getMethodName).append("().toString())");
                    stBuilder.append(");");

                } else if (classIsBuiltInType(subClass)) {
                    stBuilder.append("\t\t").append(NEW_TYPE_INSTANCE).append(".").append(methodName).append("(");
                    stBuilder.append(OLD_TYPE_INSTANCE).append(".").append(getMethodName).append("()");
                    stBuilder.append(");");
                } else if (subClass.getSimpleName().contains("List")) {
//                    String fieldName = methodName.substring(methodName.indexOf("set") + 3);
//                    fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

                    try {
                        Class listClass = getListClass(newClass, fieldName);

                        String oldListClassName;
                        if (classIsBuiltInType(listClass)) {
                            oldListClassName = listClass.getCanonicalName();
                        } else if (listClass.getCanonicalName().contains("Enum")) {
                            oldListClassName = getNewClassWithOldTypePackage(listClass, oldClassName);
                        } else {
                            oldListClassName = getNewClassWithOldTypePackage(listClass, oldClassName);

                            AbstractBuilder builder = AbstractBuilderFactory.getBuilder(listClass,
                                    oldListClassName, outputDirectory, packageName, callSet);
                            builder.build();

                        }

                        stBuilder.append("\t\tfor (").append(oldListClassName).append(" oldObj : ").append(OLD_TYPE_INSTANCE).append(".").append(getMethodName).append("()) {\n");

                        if (classIsBuiltInType(listClass)) {
                            stBuilder.append("\t\t\t").append(NEW_TYPE_INSTANCE).append(".").append(getMethodName).append("().add(oldObj);\n");
                        } else if (listClass.getCanonicalName().contains("Enum")) {
                            stBuilder.append("\t\t\t").append(NEW_TYPE_INSTANCE).append(".").append(getMethodName).append("().add(")
                                    .append(listClass.getCanonicalName()).append(".fromValue(oldObj.toString()));\n");
                        } else {
                            stBuilder.append("\t\t\t").append(listClass.getSimpleName()).append("Setter setter = new ").append(listClass.getSimpleName()).append("Setter(")
                                    .append(listClass.getCanonicalName()).append(".class,oldObj);\n");
                            stBuilder.append("\t\t\tsetter.set();\n");
                            stBuilder.append("\t\t\t").append(listClass.getCanonicalName()).append(" newObj = setter.getNewTypeInstance();\n");
                            stBuilder.append("\t\t\t").append(NEW_TYPE_INSTANCE).append(".").append(getMethodName).append("().add(newObj);\n");
                        }
                        stBuilder.append("\t\t}\n");
                    } catch (NoSuchFieldException ex) {
                        stBuilder.append("\t\tneeds_implementation\n\n");
                    }
                } else {
                    if (MigrationUtility.classHasSubclasses(subClass)) {
                        stBuilder.append("\t\t").append(subClass.getSimpleName()).append("Setter setter = ").append(subClass.getSimpleName()).append("SetterFactory.getSetter(")
                                .append(OLD_TYPE_INSTANCE).append(".").append(getMethodName).append("());\n");
                    } else {
                        stBuilder.append("\t\t").append(subClass.getSimpleName()).append("Setter setter = new ").append(subClass.getSimpleName()).append("Setter(");
                        stBuilder.append(subClass.getCanonicalName()).append(".class,");
                        stBuilder.append(OLD_TYPE_INSTANCE).append(".").append(getMethodName).append("());\n");
                    }

                    stBuilder.append("\t\tsetter.set();\n");
                    stBuilder.append("\t\t").append(NEW_TYPE_INSTANCE).append(".").append(methodName).append("(");
                    stBuilder.append("setter.").append(ABSTRACT_SETTER_GET_NEW_TYPE_METHOD).append("()");

                    AbstractBuilder builder = AbstractBuilderFactory.getBuilder(subClass,
                            getNewClassWithOldTypePackage(subClass, oldClassName), outputDirectory, packageName, callSet);
                    builder.build();

                    stBuilder.append(");");
                }
                stBuilder.append("\n\t}\n\n");

            }
        }

        createNewVariableSetter();
    }

    private void createNewVariableSetter() {

        stBuilder.append("\t@Override\n");
        stBuilder.append("\tpublic void set() throws MigrationException {\n");
        addNewVariableSetterContent();
        for (String setMethod : setters) {
            stBuilder.append("\t\t").append(setMethod).append("();\n");
        }

        stBuilder.append("\t}\n\n");
    }

    private boolean classIsBuiltInType(Class clazz) {
        String simpleName = clazz.getSimpleName();
        return (clazz.isPrimitive() || simpleName.equals("BigInteger") || simpleName.equals("Double")
                || simpleName.equals("Integer") || simpleName.equals("String")
                || simpleName.equals("XMLGregorianCalendar") || simpleName.equals("Boolean"));
    }

    protected void addNewVariableSetterContent() {

    }

    @Override
    protected void completeClass() {
        stBuilder.append("}");
    }
}
