/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.apollo.javaclassmigrator.builder;

import edu.pitt.apollo.javaclassmigrator.exception.BuilderException;
import edu.pitt.apollo.javaclassmigrator.util.MigrationUtility;

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
        if (superClass != null && !superClass.getSimpleName().equals("Object")) {
            extendedSetterClassName = superClass.getSimpleName() + "Setter";
        } else {
            extendedSetterClassName = ABSTRACT_SETTER_CLASS_NAME;
        }
        classDefinitionBuilder.append("public class ");
    }

    @Override
    protected String getSetterName() {
        return newClass.getSimpleName() + "Setter";
    }

    @Override
    protected String getClassNameForCallSet() {
        return getStandardClassCallName(newClass);
    }

    @Override
    protected void buildClassDefinition() {

        String newClassName = newClass.getCanonicalName();
        stBuilder.append(setterName).append(" extends ");

        if (isClassIsAbstract(extendedSetterClassName)) {
            setClassAbstract();
        }

        stBuilder.append(extendedSetterClassName).append("<");
        stBuilder.append(newClassName).append(">").append(" {\n\n");

        stBuilder.append("\tpublic ").append(newClass.getSimpleName()).append("Setter(")
                .append("Class<").append(newClassName).append("> newTypeClass, ")
                .append("Object ").append(OLD_TYPE_INSTANCE).append(") throws MigrationException {\n");
        stBuilder.append("\t\tsuper(").append("newTypeClass").append(", ").append(OLD_TYPE_INSTANCE).append(");\n");
        stBuilder.append("\n\t}\n\n");
    }

    @Override
    protected void buildMethods() throws BuilderException {
        Field[] allFields = newClass.getDeclaredFields();

        for (int i = 0; i < allFields.length; i++) {
            Field field = allFields[i];
            String fieldName = field.getName();
            String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            if (!getMethodName.contains("Class")) {

                String setMethodName = getMethodName.replaceFirst("get", "set");
                Method method = null;
                // try to find the accessor method matching the field
                try {
                    // first try exact match
                    method = newClass.getMethod(getMethodName);
                } catch (NoSuchMethodException ex) {
                    // next try "is" method instead of "get"
                    try {
                        getMethodName = getMethodName.replaceFirst("get", "is");
                        method = newClass.getMethod(getMethodName);
                    } catch (NoSuchMethodException ex1) {
                        // finally, try to match field name and method spelling (case insensitive)
                        String fieldNameLc = fieldName.toLowerCase();
                        Method[] methods = newClass.getDeclaredMethods();
                        boolean foundMethod = false;
                        for (Method testMethod : methods) {
                            String testMethodName = testMethod.getName();
                            if (("get" + fieldNameLc).equals(testMethodName.toLowerCase())
                                    || ("is" + fieldName).equals(testMethodName.toLowerCase())) {
                                getMethodName = testMethodName;
                                method = testMethod;
                                foundMethod = true;
                                break;
                            }
                        }

                        if (!foundMethod) {
                            warnNoSetterMethodCanBeCreated(newClass, getMethodName);
                            continue;
                        }
                    }
                }

                setters.add(setMethodName);

                if (classIsAbstract) {
                    setAbstractMethodName(setMethodName);
                    continue;
                }

                Class subClass = method.getReturnType();
                if (subClass.getName().toLowerCase().contains("enum")) {
                    stBuilder.append("\tprotected void ").append(setMethodName).append("() throws MigrationException {\n");
                    stBuilder.append("\t\tif (((").append(oldClassName).append(") ").append(OLD_TYPE_INSTANCE).append(").").append(getMethodName).append("() != null) {\n");
                    stBuilder.append("\t\t\t").append(NEW_TYPE_INSTANCE).append(".").append(setMethodName).append("(");
                    stBuilder.append(subClass.getCanonicalName()).append(".valueOf(((").append(oldClassName).append(") ").append(OLD_TYPE_INSTANCE).append(").").append(getMethodName).append("().toString())");
                    stBuilder.append(");\n");
                    stBuilder.append("\t\t}");
                    stBuilder.append("\n\t}\n\n");
                } else if (classIsBuiltInType(subClass)) {

                    try {
                        Class oldClass = Class.forName(oldClassName);
                        try {
                            Method mth = oldClass.getDeclaredMethod(getMethodName);
                            stBuilder.append("\tprotected void ").append(setMethodName).append("() throws MigrationException {\n");

                            stBuilder.append("\t\t").append(NEW_TYPE_INSTANCE).append(".").append(setMethodName).append("(");
                            stBuilder.append("((").append(oldClassName).append(") ").append(OLD_TYPE_INSTANCE).append(").").append(getMethodName).append("()");
                            stBuilder.append(");");
                            stBuilder.append("\n\t}\n\n");

                        } catch (NoSuchMethodException ex) {
                            // method not found, make abstract
                            setAbstractMethodName(setMethodName);
                            setClassAbstract();
                        }
                    } catch (ClassNotFoundException ex) {
                        // class not found, make abstract
                        setAbstractMethodName(setMethodName);
                        setClassAbstract();
                    }

                } else if (subClass.getSimpleName().contains("List")) {

                    Class oldClass = null;
                    try {
                        oldClass = Class.forName(oldClassName);
                    } catch (ClassNotFoundException ex) {
                        //this shouldn't happen since this has already been checked
                    }

                    // does old class have same field?
                    boolean oldMethodIsList = true;
                    Class oldListClass;
                    String oldObjectReference = "oldObj";

                    try {
                        Field oldField = oldClass.getDeclaredField(fieldName);
                        // is old method a list?
                        if (!oldField.getType().getSimpleName().contains("List")) {
                            oldMethodIsList = false;
                            oldListClass = oldField.getType();
                            oldObjectReference = OLD_TYPE_INSTANCE;
                        } else {
                            oldListClass = getListClass(oldClass, fieldName);
                        }
                    } catch (NoSuchFieldException ex) {
                        // can't implement this list setter
                        setAbstractMethodName(setMethodName);
                        setClassAbstract();
                        continue;
                    }

                    stBuilder.append("\tprotected void ").append(setMethodName).append("() throws MigrationException {\n");

                    Class listClass = getListClass(newClass, fieldName);

                    String oldListClassName;
                    if (classIsBuiltInType(listClass)) {
                        oldListClassName = listClass.getCanonicalName();
                    } else if (listClass.getCanonicalName().contains("Enum")) {
                        oldListClassName = oldListClass.getCanonicalName();
                    } else {
                        oldListClassName = oldListClass.getCanonicalName();

                        AbstractBuilder builder = AbstractBuilderFactory.getBuilder(listClass,
                                oldListClassName, outputDirectory, packageName, callSet);
                        builder.build();

                    }

                    if (oldMethodIsList) {
                        stBuilder.append("\t\tfor (").append(oldListClassName).append(" oldObj : ((").append(oldClassName).append(") ").append(OLD_TYPE_INSTANCE).append(").").append(getMethodName).append("()) {\n");
                    }
                    if (classIsBuiltInType(listClass)) {
                        stBuilder.append("\t\t\t").append(NEW_TYPE_INSTANCE).append(".").append(getMethodName).append("().add(").append(oldObjectReference).append(");\n");
                    } else if (listClass.getCanonicalName().contains("Enum")) {
                        stBuilder.append("\t\t\t").append(NEW_TYPE_INSTANCE).append(".").append(getMethodName).append("().add(")
                                .append(listClass.getCanonicalName()).append(".valueOf(").append(oldObjectReference).append(".toString()));\n");
                    } else {

                        String className;
                        if (isClassIsAbstract(listClass)) {
                            className = listClass.getSimpleName() + "SetterImpl";
                        } else {
                            className = listClass.getSimpleName() + "Setter";
                        }

                        stBuilder.append("\t\t\t").append(listClass.getSimpleName()).append("Setter setter = new ").append(className).append("(");
                        if (oldMethodIsList) {
                            stBuilder.append(listClass.getCanonicalName()).append(".class,").append(oldObjectReference).append(");\n");
                        } else {
                            stBuilder.append(listClass.getCanonicalName()).append(".class,((").append(oldClassName).append(") ").append(oldObjectReference).append(").").append(getMethodName).append("());\n");
                        }
                        stBuilder.append("\t\t\tsetter.set();\n");
                        stBuilder.append("\t\t\t").append(listClass.getCanonicalName()).append(" newObj = setter.getNewTypeInstance();\n");
                        stBuilder.append("\t\t\t").append(NEW_TYPE_INSTANCE).append(".").append(getMethodName).append("().add(newObj);\n");
                    }
                    if (oldMethodIsList) {
                        stBuilder.append("\t\t}\n");
                    }
                    stBuilder.append("\n\t}\n\n");

                } else {

                    AbstractBuilder builder = AbstractBuilderFactory.getBuilder(subClass,
                            getNewClassWithOldTypePackage(subClass, oldClassName), outputDirectory, packageName, callSet);
                    builder.build();

                    stBuilder.append("\tprotected void ").append(setMethodName).append("() throws MigrationException {\n");

                    boolean subClassHasSubclasses = MigrationUtility.classHasSubclasses(subClass);
                    String tabs = "\t\t";
                    if (subClassHasSubclasses) {
                        tabs = "\t\t\t";
                        stBuilder.append("\t\tif (").append("((").append(oldClassName).append(") ").append(OLD_TYPE_INSTANCE).append(").").append(getMethodName).append("() != null) {\n");
                        stBuilder.append(tabs).append(subClass.getSimpleName()).append("Setter setter = ").append(subClass.getSimpleName()).append("SetterFactory.getSetter(")
                                .append("((").append(oldClassName).append(") ").append(OLD_TYPE_INSTANCE).append(").").append(getMethodName).append("());\n");
                    } else {
                        String className;
                        if (isClassIsAbstract(subClass)) {
                            className = subClass.getSimpleName() + "SetterImpl";
                        } else {
                            className = subClass.getSimpleName() + "Setter";
                        }

                        stBuilder.append(tabs).append(subClass.getSimpleName()).append("Setter setter = new ").append(className).append("(");
                        stBuilder.append(subClass.getCanonicalName()).append(".class,");
                        stBuilder.append("((").append(oldClassName).append(") ").append(OLD_TYPE_INSTANCE).append(").").append(getMethodName).append("());\n");
                    }

                    stBuilder.append(tabs).append("setter.set();\n");
                    stBuilder.append(tabs).append(NEW_TYPE_INSTANCE).append(".").append(setMethodName).append("(");
                    stBuilder.append("setter.").append(ABSTRACT_SETTER_GET_NEW_TYPE_METHOD).append("()");

                    stBuilder.append(");");
                    if (subClassHasSubclasses) {
                        stBuilder.append("\n\t\t}\n");
                    }

                    stBuilder.append("\n\t}\n\n");

                }

            }
        }

        createNewVariableSetter();
    }

    private void createNewVariableSetter() {

        stBuilder.append("\t@Override\n");
        stBuilder.append("\tpublic void set() throws MigrationException {\n");
        stBuilder.append("\t\tif (oldTypeInstance != null) {\n");
        if (!extendedSetterClassName.equals(ABSTRACT_SETTER_CLASS_NAME)) {
            stBuilder.append("\t\t\tsuper.set();\n");
        }
        addNewVariableSetterContent();
        for (String setMethod : setters) {
            stBuilder.append("\t\t\t").append(setMethod).append("();\n");
        }
        stBuilder.append("\t\t}\n");

        stBuilder.append("\t}\n\n");
    }

    private boolean classIsBuiltInType(Class clazz) {
        String simpleName = clazz.getSimpleName();
        return (clazz.isPrimitive() || simpleName.equals("BigInteger") || simpleName.equals("Double")
                || simpleName.equals("Integer") || simpleName.equals("String")
                || simpleName.equals("XMLGregorianCalendar") || simpleName.equals("Boolean"));
    }

    // Sub classes need this method
    protected void addNewVariableSetterContent() {

    }

    @Override
    protected void completeClass() {
        stBuilder.append("}");
        stBuilder.insert(0, classDefinitionBuilder);
        stBuilder.insert(0, "package " + packageName + ";\n\n");
    }
}
