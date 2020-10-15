import tanvd.kosogor.proxy.publishJar
import tanvd.kosogor.proxy.publishPlugin

group = rootProject.group
version = rootProject.version

dependencies {
    api(project(":reflekt"))

    implementation(kotlin("compiler-embeddable"))

    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(kotlin("gradle-plugin-api"))

    implementation("net.lingala.zip4j", "zip4j", "2.6.1")

}

publishPlugin {
    id = "io.reflekt"
    displayName = "Reflekt"
    implementationClass = "io.reflekt.plugin.ReflektPlugin"
    version = project.version.toString()
}

publishJar {
    publication {
        artifactId = "io.reflekt.gradle.plugin"
    }
}
