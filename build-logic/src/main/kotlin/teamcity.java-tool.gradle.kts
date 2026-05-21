
plugins {
    id ("teamcity.shared-configuration")
    id ("org.gradle.java-test-fixtures")
}

repositories {
    mavenCentral()
}

tasks {
    val java8Compiler = project.javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    compileJava {
        javaCompiler = java8Compiler
    }

    compileTestFixturesJava {
        javaCompiler= java8Compiler
    }

    test {
        jvmArgs ("--add-opens")
        jvmArgs ("jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED")
    }
}
