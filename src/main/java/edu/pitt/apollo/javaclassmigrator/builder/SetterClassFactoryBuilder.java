package edu.pitt.apollo.javaclassmigrator.builder;

import edu.pitt.apollo.javaclassmigrator.exception.BuilderException;
import edu.pitt.apollo.javaclassmigrator.util.MigrationUtility;

import java.util.Set;

/**
 * Created by nem41 on 11/23/16.
 */
public class SetterClassFactoryBuilder extends AbstractBuilder {

    public SetterClassFactoryBuilder(Class newClass, String oldClassName, String outputDirectory, String packageName, Set<String> callSet) {
        super(newClass, oldClassName, outputDirectory, packageName, callSet);
    }

    @Override
    protected String getSetterName() {
        return newClass.getSimpleName() + "SetterFactory";
    }

    @Override
    protected String getClassNameForCallSet() {
        return getClassCallNameForFactory(newClass);
    }

    @Override
    protected void buildClassDefinition() {
        stBuilder.append("package ").append(packageName).append(";\n\n");
        stBuilder.append("public class ").append(setterName).append(" {\n\n");
    }

    @Override
    protected void buildMethods() throws BuilderException {

        if (!classSetterExists(newClass)) {
            BaseTypeSetterClassBuilder builder = new BaseTypeSetterClassBuilder(newClass,
                    getNewClassWithOldTypePackage(newClass, oldClassName), outputDirectory, packageName, callSet);
            builder.build();
        }

        stBuilder.append("\tpublic static ").append(newClass.getSimpleName()).append("Setter getSetter(Object oldTypeInstance) throws MigrationException {\n\n");

        Set<Class> subClasses = MigrationUtility.getSubclassesForClass(newClass);
        for (Class subClass : subClasses) {
            String oldClassTypeName = getNewClassWithOldTypePackage(subClass, oldClassName);
            AbstractBuilder builder = AbstractBuilderFactory.getBuilder(subClass,
                    oldClassTypeName, outputDirectory, packageName, callSet);
            builder.build();


            stBuilder.append("\t\tif (oldTypeInstance instanceof ").append(oldClassTypeName).append(") {\n\n");

            if (MigrationUtility.classHasSubclasses(subClass)) {
                stBuilder.append("\t\t\treturn ").append(subClass.getSimpleName() + "SetterFactory.getSetter(oldTypeInstance);\n");
            } else {
                String className;
                if (isClassIsAbstract(subClass)) {
                    className = subClass.getSimpleName() + "SetterImpl";
                } else {
                    className = subClass.getSimpleName() + "Setter";
                }
                stBuilder.append("\t\t\treturn new ").append(className).append("(").append(subClass.getCanonicalName())
                        .append(".class, oldTypeInstance);\n");
            }
            stBuilder.append("\t\t}\n\n");
        }

        stBuilder.append("\t\tthrow new UnsupportedTypeException(\"Type \" + oldTypeInstance.getClass().getCanonicalName() + \" is not supported\");\n\n");
        stBuilder.append("\t}\n\n");
    }

    @Override
    protected void completeClass() {
        stBuilder.append("\n}\n\n");
    }

}
