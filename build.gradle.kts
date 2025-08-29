plugins {
    id("java")
}

group = "com.jrsfleming"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.slf4j:slf4j-simple:2.0.16")

    implementation("org.testcontainers:testcontainers:1.19.0")
    implementation("org.testcontainers:junit-jupiter:1.19.0")



}

tasks.test {
    useJUnitPlatform()
}