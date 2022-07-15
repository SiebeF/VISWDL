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
    // https://mavenlibs.com/maven/dependency/ai.djl/api
    implementation("ai.djl:api:0.18.0")
    // https://mavenlibs.com/maven/dependency/ai.djl/basicdataset
    implementation("ai.djl:basicdataset:0.18.0")
    // https://mavenlibs.com/maven/dependency/ai.djl.mxnet/mxnet-engine
    implementation("ai.djl.mxnet:mxnet-engine:0.18.0")
    // https://mavenlibs.com/maven/dependency/ai.djl.mxnet/mxnet-model-zoo
    implementation("ai.djl.mxnet:mxnet-model-zoo:0.18.0")
    // https://mavenlibs.com/maven/dependency/org.slf4j/slf4j-simple
    implementation("org.slf4j:slf4j-simple:1.7.36")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}