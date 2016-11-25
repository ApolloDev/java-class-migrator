package edu.pitt.apollo.javaclassmigrator.builder;

import edu.pitt.apollo.javaclassmigrator.util.MigrationUtility;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nem41 on 11/23/16.
 */
public class AbstractBuilderFactory {

    public static AbstractBuilder getBuilder(Class newClass, String oldClassName, String outputDirectory, String packageName) {
        return getBuilder(newClass, oldClassName, outputDirectory, packageName, new HashSet<>());
    }

    protected static AbstractBuilder getBuilder(Class newClass, String oldClassName, String outputDirectory, String packageName, Set<String> callSet) {
        if (MigrationUtility.classHasSubclasses(newClass)) {
            return new SetterClassFactoryBuilder(newClass, oldClassName, outputDirectory, packageName, callSet);
        } else {
            return new SetterClassBuilder(newClass, oldClassName, outputDirectory, packageName, callSet);
        }
    }

}
