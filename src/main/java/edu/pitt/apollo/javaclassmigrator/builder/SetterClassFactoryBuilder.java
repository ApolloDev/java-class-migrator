package edu.pitt.apollo.javaclassmigrator.builder;

import edu.pitt.apollo.javaclassmigrator.exception.OldClassTypeNotFoundException;
import edu.pitt.apollo.javaclassmigrator.util.MigrationUtility;

import java.io.FileNotFoundException;
import java.util.Set;

/**
 * Created by nem41 on 11/23/16.
 */
public class SetterClassFactoryBuilder extends AbstractBuilder {

    public SetterClassFactoryBuilder(Class newClass, Class oldClass, String outputDirectory, String packageName) {
        super(newClass, oldClass, outputDirectory, packageName);
    }

    @Override
    protected void buildClassDefinition() {
        stBuilder.append("package ").append(packageName).append(";\n\n");
        stBuilder.append("public class ").append(newClass.getSimpleName()).append("SetterFactory {\n\n");
    }

    @Override
    protected void buildMethods() throws FileNotFoundException {

        if (!classSetterExists(newClass)) {
            try {
                BaseTypeSetterClassBuilder builder = new BaseTypeSetterClassBuilder(newClass,
                        getNewClassWithOldTypePackage(newClass, oldClass), outputDirectory, packageName);
                builder.build();
            } catch (OldClassTypeNotFoundException ex) {
                warnNoSetterClassCanBeCreated(newClass);
            }
        }

        stBuilder.append("\tpublic static ").append(newClass.getSimpleName()).append("Setter getSetter(Object oldTypeInstance) throws MigrationException {\n\n");

        Set<Class> subClasses = MigrationUtility.getSubclassesForClass(newClass);
        for (Class subClass : subClasses) {
            Class oldClassType;
            try {
                oldClassType = getNewClassWithOldTypePackage(subClass, oldClass);
            } catch (OldClassTypeNotFoundException ex) {
                continue;
            }
            if (!classSetterExists(subClass)) {
                SubTypeSetterClassBuilder builder = new SubTypeSetterClassBuilder(newClass, subClass,
                        oldClassType, outputDirectory, packageName);
                builder.build();
            }

            stBuilder.append("\t\tif (oldTypeInstance instanceof ").append(subClass.getCanonicalName()).append(") {\n\n");
            stBuilder.append("\t\t\treturn new ").append(subClass.getSimpleName() + "Setter(").append(subClass.getCanonicalName())
                    .append(".class, (").append(oldClassType.getCanonicalName()).append(") oldTypeInstance);\n");
            stBuilder.append("\t\t}\n\n");
        }

        stBuilder.append("\t\tthrow new UnsupportedTypeException(\"Type \" + oldTypeInstance.getClass().getCanonicalName() + \" is not supported\");\n\n");
        stBuilder.append("\t}\n\n");
    }

    @Override
    protected void completeClass() {
        stBuilder.append("\n}\n\n");
    }

    @Override
    public void printSetterFile() throws FileNotFoundException {
        writeClassTofile(stBuilder.toString(),
                outputDirectory, newClass.getSimpleName() + "SetterFactory");
    }
}
