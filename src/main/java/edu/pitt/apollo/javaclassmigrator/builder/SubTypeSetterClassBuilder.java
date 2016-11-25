package edu.pitt.apollo.javaclassmigrator.builder;

import java.util.Set;

/**
 * Created by nem41 on 11/25/16.
 */
public class SubTypeSetterClassBuilder extends SetterClassBuilder {

    private Class superClass;

    public SubTypeSetterClassBuilder(Class superClass, Class newClass, String oldClassName, String outputDirectory, String packageName, Set<String> callSet) {
        super(newClass, oldClassName, outputDirectory, packageName, callSet);
        this.superClass = superClass;
        extendedSetterClassName = superClass.getSimpleName() + "Setter";
    }

    @Override
    protected void addNewVariableSetterContent() {
        stBuilder.append("\t\tsuper.set();\n");
    }
}
