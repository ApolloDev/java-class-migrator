package edu.pitt.apollo.javaclassmigrator.builder;

import edu.pitt.apollo.javaclassmigrator.exception.BuilderException;

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

        stBuilder.append(newClass.getSimpleName()).append("Setter<T extends ").append(newClassName)
        .append("> extends ");
        stBuilder.append(extendedSetterClassName).append("<T>").append(" {\n\n");

        stBuilder.append("\tpublic ").append(newClass.getSimpleName()).append("Setter(")
                .append("Class<T> newTypeClass, Object ").append(OLD_TYPE_INSTANCE).append(") throws MigrationException {\n");
        stBuilder.append("\t\tsuper(").append("newTypeClass").append(", ").append(OLD_TYPE_INSTANCE).append(");\n");
        stBuilder.append("\n\t}\n\n");
    }

    @Override
    protected void buildMethods() throws BuilderException {
        super.buildMethods();
        stBuilder.append("\t@Override\n");
        stBuilder.append("\tpublic T ").append(ABSTRACT_SETTER_GET_NEW_TYPE_METHOD).append("() {\n");
        stBuilder.append("\t\treturn ").append(NEW_TYPE_INSTANCE).append(";\n");
        stBuilder.append("\t}\n\n");
    }
}
