import java.util.zip.ZipFile

plugins {
    java
    id("net.minecraftforge.gradle") version "6.0.54"
    id("org.spongepowered.mixin") version "0.7.38"
}

group = "com.bettercontent"
version = property("mod_version") as String
base { archivesName.set(property("artifact_name") as String) }
java { toolchain.languageVersion.set(JavaLanguageVersion.of(17)) }
val gameTestFixtures = sourceSets.create("gameTestFixtures")

// CI and fresh-release builds provide verified runtime JARs explicitly.
// Ordinary local builds retain the canonical sibling build/libs convention.
fun betterContentJar(repository: String, artifact: String): java.io.File {
    val directory = providers.environmentVariable("BC_CUSTOM_MOD_JAR_DIR").orNull
    require(directory == null || directory.isNotBlank()) { "BC_CUSTOM_MOD_JAR_DIR must not be blank" }
    val jar = if (directory == null) file("../$repository/build/libs/$artifact") else file(directory).resolve(artifact)
    require(jar.isFile) {
        "Missing Better Content provider $artifact at $jar; prepare BC_CUSTOM_MOD_JAR_DIR or build $repository first"
    }
    return jar
}

fun bumblezoneDevelopmentJar(): java.io.File {
    val source = providers.environmentVariable("BC_BUMBLEZONE_CULTIVARS_SOURCE").orNull
    require(source == null || source.isNotBlank()) { "BC_BUMBLEZONE_CULTIVARS_SOURCE must not be blank" }
    val jar = if (source == null) file("../bumblezone-cultivars/build/development-dependencies/bumblezone-mapped.jar")
        else file(source).resolve("build/development-dependencies/bumblezone-mapped.jar")
    require(jar.isFile) {
        "Missing mapped Bumblezone runtime at $jar; run bumblezone-cultivars remapBumblezoneDevelopment first"
    }
    return jar
}

minecraft {
    mappings("official", property("minecraft_version") as String)
    copyIdeResources = true
    runs {
        configureEach {
            workingDirectory(project.file("run"))
            property("forge.logging.console.level", "info")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            mods { create(property("mod_id") as String) { source(sourceSets.main.get()) } }
        }
        create("client")
        create("server") { arg("--nogui") }
        create("gameTestServer") {
            mods { named(project.property("mod_id") as String) { source(gameTestFixtures) } }
            workingDirectory(project.file("run-gametest"))
            property("forge.enableGameTest", "true")
            property("forge.gameTestServer", "true")
            property("forge.enabledGameTestNamespaces", property("mod_id") as String)
            arg("--nogui")
        }
    }
}

repositories {
    maven("https://maven.minecraftforge.net")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://www.cursemaven.com") { content { includeGroup("curse.maven") } }
    mavenCentral()
    ivy {
        name = "bumblezoneCultivarsLocal"
        url = uri(betterContentJar("bumblezone-cultivars", "bumblezone-cultivars-0.1.0.jar").parentFile)
        patternLayout { artifact("[artifact]-[revision].[ext]") }
        metadataSources { artifact() }
        content { includeGroup("bettercontent.local") }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:${property("minecraft_version")}-${property("forge_version")}")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly(fg.deobf("curse.maven:rats-323596:5904296"))
    compileOnly(fg.deobf("curse.maven:pretty-pipes-376737:4769646"))
    compileOnly(fg.deobf("curse.maven:citadel-331936:5633260"))
    compileOnly(fg.deobf("bettercontent.local:bumblezone-cultivars:0.1.0"))
    runtimeOnly(fg.deobf("curse.maven:rats-323596:5904296"))
    runtimeOnly(fg.deobf("curse.maven:pretty-pipes-376737:4769646"))
    runtimeOnly(fg.deobf("curse.maven:citadel-331936:5633260"))
    runtimeOnly(files(bumblezoneDevelopmentJar()))
    runtimeOnly(fg.deobf("bettercontent.local:bumblezone-cultivars:0.1.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.named<Jar>("jar") {
    // MixinGradle contributes the refmap while processResources copies it for
    // development GameTests; keep one deterministic JAR entry.
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    finalizedBy("reobfJar")
}
val stageRuntimeJar by tasks.registering(Copy::class) {
    dependsOn(tasks.named("reobfJar"))
    from(layout.buildDirectory.file("reobfJar/output.jar"))
    into(layout.buildDirectory.dir("libs"))
    rename { "${base.archivesName.get()}-$version.jar" }
}
tasks.named("assemble") { dependsOn(stageRuntimeJar) }
tasks.withType<JavaCompile>().configureEach { options.release.set(17) }
tasks.test { useJUnitPlatform() }
tasks.register("verifyFast") { dependsOn(tasks.named("check")) }
tasks.register("verifyFull") { dependsOn(tasks.named("verifyFast"), tasks.named("runGameTestServer")) }
tasks.processResources {
    // The Mixin processor writes the refmap under build/tmp.  Copy it into
    // development and GameTest resources as well as the reobfuscated JAR;
    // otherwise redirects silently do not apply in provider verification.
    dependsOn(tasks.compileJava)
    from(layout.buildDirectory.file("tmp/compileJava/ratlantis_logistics.refmap.json"))
    val props = mapOf("minecraft_version" to project.property("minecraft_version"), "forge_version" to project.property("forge_version"), "mod_id" to project.property("mod_id"), "mod_name" to project.property("mod_name"), "mod_version" to project.property("mod_version"))
    inputs.properties(props)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) { expand(props) }
}
mixin {
    add(sourceSets.main.get(), "ratlantis_logistics.refmap.json")
    config("ratlantis_logistics.mixins.json")
}

val verifyRuntimeMixinRefmap by tasks.registering {
    group = "verification"
    description = "Requires the release JAR to contain the production mappings used by the Rats trust mixin."
    dependsOn(stageRuntimeJar)
    doLast {
        val runtimeJar = layout.buildDirectory.file("libs/${base.archivesName.get()}-$version.jar").get().asFile
        ZipFile(runtimeJar).use { zip ->
            val entry = zip.getEntry("ratlantis_logistics.refmap.json")
                ?: throw GradleException("Runtime JAR is missing ratlantis_logistics.refmap.json: $runtimeJar")
            val text = zip.getInputStream(entry).bufferedReader().use { it.readText() }
            check(text.contains("m_8037_") && text.contains("m_204117_")) {
                "Runtime refmap lacks production mappings for WildRatTrustMixin: $runtimeJar"
            }
        }
    }
}

tasks.named("verifyFull") { dependsOn(verifyRuntimeMixinRefmap) }

apply(from = "gametest/execution-evidence.gradle")
