plugins {
    // database
    id("org.flywaydb.flyway") version "9.16.1"
    id("nu.studer.jooq") version "9.0"

    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.0.1"

}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // kafka
    implementation("org.springframework.kafka:spring-kafka")
    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // database
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.flywaydb:flyway-core")
    // driver for jOOQ code generation runtime
    jooqGenerator("org.postgresql:postgresql:42.7.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


}


kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

openApiGenerate {
	generatorName.set("kotlin-spring")
	inputSpec.set("$rootDir/src/main/resources/openapi/api.yaml")
	outputDir.set("$buildDir/generated")
	modelPackage.set("com.example.generated.model")
	apiPackage.set("com.example.generated.api")
	configOptions.set(mapOf(
		"interfaceOnly" to "true",
		"useSpringBoot3" to "true",
		"useBeanValidation" to "true",
		"documentationProvider" to "none"
	))
}

sourceSets.main {
    java.srcDirs(
        "$buildDir/generated/src/main/kotlin",
        "$buildDir/generated-src/jooq/main"
    )
}


// Flyway configuration for local development
flyway {
    url = "jdbc:postgresql://localhost:5432/incentivize"
    user = "incentivize"
    password = "cred"
    schemas = arrayOf("public")
}

// jOOQ code generation configuration
jooq {
    val jooqVersion: String by project
    version.set(jooqVersion)
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/incentivize"
                    user = "incentivize"
                    password = "cred"
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        inputSchema = "public"
                    }
                    generate.apply {
                        isPojos = true
                        isDaos = true
                    }
                    target.apply {
                        packageName = "com.example.jooq.generated"
                        directory = "$buildDir/generated-src/jooq/main"
                    }
                }
            }
        }
    }
}

tasks.compileKotlin {
	dependsOn("openApiGenerate")
}


tasks.withType<Test> {
	useJUnitPlatform()
}
