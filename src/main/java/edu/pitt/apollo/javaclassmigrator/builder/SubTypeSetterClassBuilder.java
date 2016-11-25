package edu.pitt.apollo.javaclassmigrator.builder;

/**
 * Created by nem41 on 11/25/16.
 */
public class SubTypeSetterClassBuilder extends SetterClassBuilder {

    private Class superClass;

    public SubTypeSetterClassBuilder(Class superClass, Class newClass, Class oldClass, String outputDirectory, String packageName) {
        super(newClass, oldClass, outputDirectory, packageName);
        this.superClass = superClass;
        extendedSetterClassName = superClass.getSimpleName() + "Setter";
    }

    @Override
    protected void addNewVariableSetterContent() {
        stBuilder.append("\t\tsuper.set();\n");
    }
}
