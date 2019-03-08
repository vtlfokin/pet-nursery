import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.11"
}

group = "com.example.vtlfokin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.axonframework:axon-configuration:4.0.3")
    compile("io.ktor:ktor-server-core:1.1.1")
    compile("io.ktor:ktor-server-netty:1.1.1")
    compile("io.ktor:ktor-websockets:1.1.1")
    compile("org.slf4j:slf4j-simple:1.7.25")
    compile("org.postgresql:postgresql:42.2.5")
    compile("javax.inject:javax.inject:1")
    compile("com.google.code.gson:gson:2.8.5")
    compile("org.jetbrains.exposed:exposed:0.12.2")

    testCompile("org.axonframework:axon-test:4.0.3")
    testCompile("junit:junit:4.12")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:1.3.11")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}