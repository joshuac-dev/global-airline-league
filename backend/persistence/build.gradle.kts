plugins {
    kotlin("plugin.serialization")
    id("org.flywaydb.flyway") version "10.4.1"
}

configurations {
    create("flywayMigration")
}

dependencies {
    implementation(project(":backend:core"))
    
    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.jetbrains.exposed:exposed-json:0.45.0")
    
    // Database
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Flyway migrations
    implementation("org.flywaydb:flyway-core:10.4.1")
    implementation("org.flywaydb:flyway-database-postgresql:10.4.1")
    
    // For Flyway Gradle plugin
    "flywayMigration"("org.postgresql:postgresql:42.7.1")
    "flywayMigration"("org.flywaydb:flyway-database-postgresql:10.4.1")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
}

flyway {
    val dbUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/airline_league"
    val dbUser = System.getenv("DATABASE_USERNAME") ?: "airline_user"
    val dbPass = System.getenv("DATABASE_PASSWORD") ?: "airline_pass"
    
    url = dbUrl
    user = dbUser
    password = dbPass
    locations = arrayOf("classpath:db/migration")
    configurations = arrayOf("flywayMigration")
}
