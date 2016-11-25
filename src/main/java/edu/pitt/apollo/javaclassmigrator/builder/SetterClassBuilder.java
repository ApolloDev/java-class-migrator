/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.apollo.javaclassmigrator.builder;

import edu.pitt.apollo.javaclassmigrator.exception.OldClassTypeNotFoundException;
import edu.pitt.apollo.javaclassmigrator.util.MigrationUtility;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author nem41
 */
public class SetterClassBuilder extends AbstractBuilder {

    protected static final String NEW_TYPE_INSTANCE = "newTypeInstance";
    protected static final String OLD_TYPE_INSTANCE = "oldTypeInstance";
    protected String extendedSetterClassName;
    protected final List<String> setters;

    public SetterClassBuilder(Class newClass, Class oldClass, String outputDirectory, String packageName) {
        super(newClass, oldClass, outputDirectory, packageName);
        setters = new ArrayList<>();
        extendedSetterClassName = ABSTRACT_SETTER_CLASS_NAME;
    }

    @Override
    public void printSetterFile() throws FileNotFoundException {
        writeClassTofile(stBuilder.toString(),
                outputDirectory, newClass.getSimpleName() + "Setter");
    }

    @Override
    protected void buildClassDefinition() {

        String newClassName = newClass.getCanonicalName();
        String oldClassName = oldClass.getCanonicalName();

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
        Method[] allMethods = newClass.getDeclaredMethods();

        for (int i = 0; i < allMethods.length; i++) {
            Method method = allMethods[i];
            String methodName = method.getName();
            if (methodName.startsWith("get") && !methodName.contains("Class")) {

                String getMethodName = methodName;
                methodName = methodName.replaceFirst("get", "set");
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
                    String fieldName = methodName.substring(methodName.indexOf("set") + 3);
                    fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

                    try {
                        Class listClass = getListClass(newClass, fieldName);
                        Class oldListClass;
                        if (classIsBuiltInType(listClass)) {
                            oldListClass = listClass;
                        } else {
                            oldListClass = getNewClassWithOldTypePackage(listClass, oldClass);
                            if (!classSetterExists(listClass) && !listClass.getCanonicalName().equals(newClass.getCanonicalName())) {
                                // create setter class file
                                AbstractBuilder builder = AbstractBuilderFactory.getBuilder(listClass,
                                        oldListClass, outputDirectory, packageName);
                                    builder.build();
                            }
                        }

                        stBuilder.append("\t\tfor (").append(oldListClass.getCanonicalName()).append(" oldObj : ").append(OLD_TYPE_INSTANCE).append(".").append(getMethodName).append("()) {\n");

                        if (classIsBuiltInType(listClass)) {
                            stBuilder.append("\t\t\t").append(NEW_TYPE_INSTANCE).append(".").append(getMethodName).append("().add(oldObj);\n");
                        } else {
                            stBuilder.append("\t\t\t").append(listClass.getSimpleName()).append("Setter setter = new ").append(listClass.getSimpleName()).append("Setter(")
                                    .append(listClass.getCanonicalName()).append(".class,oldObj);\n");
                            stBuilder.append("\t\t\tsetter.set();\n");
                            stBuilder.append("\t\t\t").append(listClass.getCanonicalName()).append(" newObj = setter.getNewTypeInstance();\n");
                            stBuilder.append("\t\t\t").append(NEW_TYPE_INSTANCE).append(".").append(getMethodName).append("().add(newObj);\n");
                        }
                        stBuilder.append("\t\t}\n");
                    } catch (NoSuchFieldException | OldClassTypeNotFoundException ex) {
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

                    if (!classSetterExists(subClass)) {
                        try {
                            AbstractBuilder builder = AbstractBuilderFactory.getBuilder(subClass,
                                    getNewClassWithOldTypePackage(subClass, oldClass), outputDirectory, packageName);
                            builder.build();
                        } catch (OldClassTypeNotFoundException ex) {
                            warnNoSetterClassCanBeCreated(subClass);
                        }
                    }
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
                || simpleName.equals("XMLGregorianCalendar"));
    }

    protected void addNewVariableSetterContent() {

    }

    @Override
    protected void completeClass() {
        stBuilder.append("}");
    }
}
