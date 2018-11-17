package net.capellari.julien.opengl.processor

import androidx.annotation.RequiresApi
import com.squareup.kotlinpoet.FileSpec
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
        val writer = OutputStreamWriter(Files.newOutputStream(out), StandardCharsets.UTF_8)

        code.writeTo(object : Appendable {
            fun replace(str: CharSequence?): CharSequence {
                return (str ?: "null")
                        .replace("java\\.lang".toRegex(), "kotlin")
                        .replace("kotlin\\.Integer".toRegex(), "kotlin.*")

                        .replace("Integer".toRegex(), "Int")
                        .replace("Array<Short>".toRegex(), "ShortArray")
                        .replace("Array<Int>".toRegex(),   "IntArray")
                        .replace("Array<Float>".toRegex(), "FloatArray")
            }

            override fun append(str: CharSequence?): java.lang.Appendable {
                writer.append(replace(str))
                return this
            }

            override fun append(str: CharSequence?, p1: Int, p2: Int): java.lang.Appendable {
                writer.append(replace(str), p1, p2)
                return this
            }

            override fun append(c: Char): java.lang.Appendable {
                writer.append(c)
                return this
            }
        })

        writer.close()
    }
}