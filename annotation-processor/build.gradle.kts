plugins {
    id("java")
}

group = "io.amoakoagyei"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":annotations"))
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    implementation("com.google.auto.service:auto-service:1.1.1")
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}