plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
//    implementation("org.slf4j:slf4j-api:1.7.26")
//    implementation("org.slf4j:slf4j-simple:1.7.26")
//    implementation("ai.djl.pytorch:pytorch-native-auto:1.8.1")
//    // https://mavenlibs.com/maven/dependency/org.tensorflow/tensorflow
    implementation("org.tensorflow:tensorflow:1.15.0")
////    implementation("ai.djl.pytorch:pytorch-model-zoo:0.11.0")
//    // https://mavenlibs.com/maven/dependency/ai.djl.mxnet/mxnet-model-zoo
//    implementation("ai.djl.mxnet:mxnet-model-zoo:0.17.0")
//    //   implementation("org.knowm.datasets:datasets-cifar10:2.1.0")
////    implementation("ai.djl:basicdataset:0.17.0")
    implementation("net.sf.opencsv:opencsv:2.3")
    implementation("commons-io:commons-io:2.6") //dependency for file extension
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