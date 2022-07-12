plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
// https://mavenlibs.com/maven/dependency/com.palantir.safe-logging/logger
    implementation("com.palantir.safe-logging:logger:1.26.0")
    // https://mavenlibs.com/maven/dependency/ai.djl/basicdataset
    implementation("ai.djl:basicdataset:0.18.0")
    // https://mavenlibs.com/maven/dependency/ai.djl/model-zoo
    implementation("ai.djl:model-zoo:0.18.0")
// https://mavenlibs.com/maven/dependency/ai.djl.pytorch/pytorch-model-zoo
    implementation("ai.djl.pytorch:pytorch-model-zoo:0.18.0")
//    implementation(":engines:tensorflow:tensorflow-model-zoo")
// https://mavenlibs.com/maven/dependency/ai.djl.mxnet/mxnet-model-zoo
    implementation("ai.djl.mxnet:mxnet-model-zoo:0.18.0")
    // https://mavenlibs.com/maven/dependency/ai.djl/examples
    implementation("ai.djl:examples:0.6.0")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}