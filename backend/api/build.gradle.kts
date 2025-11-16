plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("com.gal.api.MainKt")
}

dependencies {
    implementation(project(":backend:core"))
    implementation(project(":backend:persistence"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.ktor.server)
    implementation(libs.logback.classic)

    testImplementation(libs.bundles.testing)
    testImplementation(libs.ktor.server.test.host)
}
