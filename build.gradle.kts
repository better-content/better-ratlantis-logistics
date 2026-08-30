plugins {
    java
    id("net.minecraftforge.gradle") version "[6.0.24,6.2)"
    id("org.spongepowered.mixin") version "0.7.+"
}

group = "com.bettercontent"
version = property("mod_version") as String
base { archivesName.set(property("artifact_name") as String) }
java { toolchain.languageVersion.set(JavaLanguageVersion.of(17)) }

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
}

dependencies {
    minecraft("net.minecraftforge:forge:${property("minecraft_version")}-${property("forge_version")}")
    compileOnly(fg.deobf("curse.maven:rats-323596:5904296"))
    compileOnly(fg.deobf("curse.maven:pretty-pipes-376737:4769646"))
    compileOnly(fg.deobf("curse.maven:citadel-331936:5633260"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.named<Jar>("jar") { finalizedBy("reobfJar") }
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
tasks.register("verifyFull") { dependsOn(tasks.named("verifyFast")) }
tasks.processResources {
    val props = mapOf("minecraft_version" to project.property("minecraft_version"), "forge_version" to project.property("forge_version"), "mod_id" to project.property("mod_id"), "mod_name" to project.property("mod_name"), "mod_version" to project.property("mod_version"))
    inputs.properties(props)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) { expand(props) }
}
mixin { config("ratlantis_logistics.mixins.json") }
