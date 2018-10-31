package net.capellari.julien.opengl

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Attribute(val name: String, val vbo: Int = 0)
        // vbo: (= 0) => pas dans le VBO (par défaut)
        //      (> 0) => taille de chaque unité