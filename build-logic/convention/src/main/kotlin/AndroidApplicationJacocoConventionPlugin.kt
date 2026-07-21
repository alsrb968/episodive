import io.jacob.episodive.configureKover
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

// 커버리지 convention plugin (Kover 기반). plugin id("episodive.android.application.jacoco")는
// 하위 호환을 위해 그대로 유지 → 모듈 build.gradle.kts 변경 불필요.
class AndroidApplicationJacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlinx.kover")

            configureKover()
        }
    }
}
