package edu.pitt.apollo.javaclassmigrator.builder;

import edu.pitt.apollo.javaclassmigrator.util.MigrationUtility;

/**
 * Created by nem41 on 11/23/16.
 */
public class AbstractBuilderFactory {

    public static AbstractBuilder getBuilder(Class newClass, Class oldClass, String outputDirectory, String packageName) {
        if (MigrationUtility.classHasSubclasses(newClass)) {
            return new SetterClassFactoryBuilder(newClass, oldClass, outputDirectory, packageName);
        } else {
            return new SetterClassBuilder(newClass, oldClass, outputDirectory, packageName);
        }
    }

}
