plugins {
    id("java")
}

group = "io.amoakoagyei"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// implementation()
//configurations.all {
//    resolutionStrategy {
//        force("com.google.guava:guava:33.0.0-jre")
//    }
//}

dependencies {
    implementation(project(":annotations"))

    annotationProcessor(project(":annotation-processor"))
    implementation(project(":annotation-processor"))

    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

tasks.test {
    useJUnitPlatform()
}