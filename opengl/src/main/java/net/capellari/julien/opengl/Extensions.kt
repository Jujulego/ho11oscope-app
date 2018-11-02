package net.capellari.julien.opengl

import net.capellari.julien.opengl.base.BaseMat
import net.capellari.julien.opengl.base.BaseVec

// Opérateurs inverses
operator fun <T : BaseVec<T>>   Float.times(vec: T): T = vec*this
operator fun <T : BaseMat<T,*>> Float.times(mat: T): T = mat*this