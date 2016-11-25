package edu.pitt.apollo.javaclassmigrator.builder;

import java.io.FileNotFoundException;
import java.util.Set;

/**
 * Created by nem41 on 11/25/16.
 */
public class BaseTypeSetterClassBuilder extends SetterClassBuilder {
    public BaseTypeSetterClassBuilder(Class newClass, String oldClassName, String outputDirectory, String packageName, Set<String> callSet) {
        super(newClass, oldClassName, outputDirectory, packageName, callSet);
    }

    @Override
    protected void buildClassDefinition() {

        String newClassName = newClass.getCanonicalName();

        stBuilder.append("package ").append(packageName).append(";\n\n");

        stBuilder.append("public class ").append(newClass.getSimpleName()).append("Setter<T extends ").append(newClassName)
        .append(", O extends ").append(oldClassName).append("> extends ");
        stBuilder.append(ABSTRACT_SETTER_CLASS_NAME).append("<T,O>").append(" {\n\n");

        stBuilder.append("\tpublic ").append(newClass.getSimpleName()).append("Setter(")
                .append("Class<T> newTypeClass, O ").append(OLD_TYPE_INSTANCE).append(") throws MigrationException {\n");
        stBuilder.append("\t\tsuper(").append("newTypeClass").append(", ").append(OLD_TYPE_INSTANCE).append(");\n");
        stBuilder.append("\n\t}\n\n");
    }

    @Override
    protected void buildMethods() throws FileNotFoundException {
        super.buildMethods();
        stBuilder.append("\t@Override\n");
        stBuilder.append("\tpublic T ").append(ABSTRACT_SETTER_GET_NEW_TYPE_METHOD).append("() {\n");
        stBuilder.append("\t\treturn ").append(NEW_TYPE_INSTANCE).append(";\n");
        stBuilder.append("\t}\n\n");
    }
}
