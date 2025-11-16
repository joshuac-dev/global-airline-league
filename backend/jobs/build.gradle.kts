plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":backend:core"))
    implementation(project(":backend:persistence"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logback.classic)
    implementation(libs.commons.csv)
    implementation(libs.dotenv.kotlin)

    testImplementation(libs.bundles.testing)
}

tasks.register<JavaExec>("importAirports") {
    group = "data-import"
    description = "Import airport data from OurAirports CSV file"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.gal.jobs.importer.AirportsImporter")
    
    // Pass environment variables to the process
    environment(System.getenv())
}
