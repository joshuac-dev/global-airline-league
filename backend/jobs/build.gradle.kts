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
    
    // Load .env file if it exists and merge with system environment variables
    val envFile = rootProject.file(".env")
    val envMap = mutableMapOf<String, String>()
    
    // First, load from .env file if it exists
    if (envFile.exists()) {
        envFile.readLines().forEach { line ->
            val trimmedLine = line.trim()
            // Skip empty lines and comments
            if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                val parts = trimmedLine.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    envMap[key] = value
                }
            }
        }
    }
    
    // Then, override with system environment variables (they take precedence)
    envMap.putAll(System.getenv())
    
    // Pass all environment variables to the process
    environment(envMap)
}
