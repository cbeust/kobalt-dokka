
import com.beust.kobalt.plugin.kotlin.kotlinProject
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.jcenter
import com.beust.kobalt.repos

val r = repos("https://dl.bintray.com/cbeust/maven")

//val pl = plugins(file(homeDir("kotlin/kobalt-retrolambda/kobaltBuild/libs/kobalt-retrolambda-0.3.jar")))

val p = kotlinProject {
    name = "kobalt-dokka"
    artifactId = name
    group = "com.beust"
    version = "0.6"

    dependencies {
//        compile(file(homeDir("kotlin/kobalt/kobaltBuild/libs/kobalt-0.367.jar")))
//         compile("com.beust:kobalt:0.367")
      compile("com.beust:kobalt-plugin-api:0.367",
              "org.jetbrains.dokka:dokka-fatjar:0.9.3")
    }

    assemble {
        mavenJars {
            fatJar = true
        }
    }

    jcenter {
        publish = true
    }
}
