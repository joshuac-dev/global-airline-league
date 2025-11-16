plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":backend:core"))
    implementation(project(":backend:persistence"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logback.classic)

    testImplementation(libs.bundles.testing)
}
