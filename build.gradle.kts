plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation("org.seleniumhq.selenium:selenium-java:4.51.0")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("io.rest-assured:rest-assured:5.5.6")
    implementation("com.codeborne:selenide:7.18.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}