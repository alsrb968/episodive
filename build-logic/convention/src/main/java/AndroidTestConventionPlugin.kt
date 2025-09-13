import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import io.jacob.episodive.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.findByType(ApplicationExtension::class.java)?.apply {
                packaging.resources {
                    excludes += "/META-INF/LICENSE.md"
                    excludes += "/META-INF/LICENSE-notice.md"
                }
            }
            extensions.findByType(LibraryExtension::class.java)?.apply {
                packaging.resources {
                    excludes += "/META-INF/LICENSE.md"
                    excludes += "/META-INF/LICENSE-notice.md"
                }
            }

            dependencies {
                "testImplementation"(project(":core:testing"))
                "androidTestImplementation"(project(":core:testing"))

                val bom = libs.findLibrary("androidx.compose.bom").get()
                "androidTestImplementation"(platform(bom))
                "androidTestImplementation"(libs.findLibrary("androidx.compose.ui.test.junit4").get())

                "testImplementation"(libs.findLibrary("androidx.test.core").get())
                "testImplementation"(libs.findLibrary("robolectric").get())
                "testImplementation"(libs.findLibrary("junit").get())
                "androidTestImplementation"(libs.findLibrary("androidx.junit").get())
                "androidTestImplementation"(libs.findLibrary("androidx.espresso.core").get())
                "testImplementation"(libs.findLibrary("kotlinx.coroutines.test").get())
                "androidTestImplementation"(libs.findLibrary("kotlinx.coroutines.test").get())
                "testImplementation"(libs.findLibrary("mockk").get())
                "androidTestImplementation"(libs.findLibrary("mockk.android").get())
                "testImplementation"(libs.findLibrary("turbine").get())
            }
        }
    }
}
