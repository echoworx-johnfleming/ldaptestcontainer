plugins {
    id("java")
}

group = "com.jrsfleming"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")

    implementation("org.testcontainers:testcontainers:1.21.4")
    implementation("org.testcontainers:junit-jupiter:1.21.4")
}

tasks.test {
    useJUnitPlatform()
}