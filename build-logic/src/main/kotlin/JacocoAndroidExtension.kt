import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import javax.inject.Inject

abstract class JacocoAndroidExtension @Inject constructor(objects: ObjectFactory) {
    val excludes: ListProperty<String> = objects.listProperty(String::class.java)
}
