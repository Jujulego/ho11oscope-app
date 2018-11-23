package net.capellari.julien.opengl.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind

@RequiresApi(26)
object Utils {
    fun getTypeName(type: TypeName): TypeName {
        return when(type) {
            IntArray::class.asTypeName() -> IntArray::class.asTypeName()
            else -> type
        }
    }

    fun inherit(processingEnv: ProcessingEnvironment, type: TypeElement, base: String) : Boolean {
        var superTypeValid = true

        val parent = type.superclass
        if(parent.kind != TypeKind.DECLARED) {
            superTypeValid = false

        } else {
            val parentType = parent as DeclaredType
            val parentEl = parentType.asElement()

            if(parentEl.kind != ElementKind.CLASS) {
                superTypeValid = false
            }
        }

        return superTypeValid && processingEnv.typeUtils.isSameType(parent, processingEnv.elementUtils.getTypeElement(base).asType())
    }

    fun writeTo(output: String, code: FileSpec) {
        var out = Paths.get(output)

        if (!Files.exists(out)) Files.createDirectory(out)
        if (code.packageName.isNotEmpty()) {
            for (packageComponent in code.packageName.split('.').dropLastWhile { it.isEmpty() }) {
                out = out.resolve(packageComponent)
            }
        }

        Files.createDirectories(out)
        out = out.resolve("${code.name}.kt")

        // Write down to file
        val buffer = StringBuffer()
        code.writeTo(buffer)

        val writer = OutputStreamWriter(Files.newOutputStream(out), StandardCharsets.UTF_8)

        writer.write(buffer
                .replace("java\\.lang".toRegex(), "kotlin")
                .replace("kotlin\\.Integer".toRegex(), "kotlin.*")
                .replace("Integer".toRegex(), "Int")
                .replace("Array<Int>".toRegex(), "IntArray")
        )

        writer.close()
    }
}