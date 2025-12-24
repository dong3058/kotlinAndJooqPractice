plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.8"
	id("io.spring.dependency-management") version "1.1.7"
	id("nu.studer.jooq") version "9.0"
}

group = "com.jooqtest"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

jooq {
	version.set("3.19.28")
	configurations {
		create("main") {
			generateSchemaSourceOnCompilation.set(false)
			jooqConfiguration.apply {
				logging = org.jooq.meta.jaxb.Logging.WARN
				jdbc.apply {
					driver = "com.mysql.cj.jdbc.Driver"
					url =  "jdbc:mysql://localhost:3306/jooqtest"
					user =  "root"
					password = "1234"
				}
				generator.apply {
					name = "org.jooq.codegen.KotlinGenerator"
					database.apply {
						name = "org.jooq.meta.mysql.MySQLDatabase"
						inputSchema = "jooqtest"
						excludes = "sys"
						includes = ".*"
					}

					generate.apply {
						isDeprecated = false
						isFluentSetters = true
						isRecords = true
						isDaos= true
						isJavaTimeTypes= true
					}
					target.apply {
						packageName = "com.jooqtest.jooq"
						directory = "build/generated-src/jooq/main"
					}
					strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
				}
			}
		}
	}
}
repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.jooq:jooq")
	jooqGenerator("com.mysql:mysql-connector-j")
	jooqGenerator("org.jooq:jooq-meta")
	jooqGenerator("org.jooq:jooq-codegen")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	runtimeOnly("com.mysql:mysql-connector-j")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	//rabbit mq
	implementation ("org.springframework.boot:spring-boot-starter-websocket")
	implementation ("org.springframework.boot:spring-boot-starter-amqp")
	implementation("com.rabbitmq:stream-client:0.15.0")
	implementation("org.springframework.amqp:spring-rabbit-stream:3.0.0")
	implementation("io.projectreactor.netty:reactor-netty")


	//implementation ("org.flywaydb:flyway-core")
	runtimeOnly ("com.h2database:h2")

	implementation("org.springframework.retry:spring-retry")
	implementation ("org.springframework.boot:spring-boot-starter-aop")

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}
sourceSets {
	main {
		java {
			srcDir("build/generated-src/jooq/main")
		}
	}
	test {
		java {
			srcDir("src/test/kotlin/com/jooqtest")
		}
	}
}
tasks.named("compileKotlin") {
	dependsOn("generateJooq")
}

tasks.named("compileTestKotlin") {
	dependsOn("generateJooq")
}


tasks.withType<Test> {
	useJUnitPlatform()
}
