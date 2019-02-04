package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.*
import net.capellari.julien.opengl.base.BaseSharedStorage
import net.capellari.julien.opengl.base.BaseUniformBlock

@UniformBlock
abstract class MatricesBlock : BaseUniformBlock() {
    // Companion
    companion object {
        val instance by lazy(this) { BaseUniformBlock.getImplementation<MatricesBlock>() }
    }

    // Attributs
    @Field open var mvpMatrix   = Mat4()
    @Field open var modelMatrix = Mat4()
    @Field open var lightMatrix = Mat4.identity()
}

@UniformBlock
abstract class StablesBlock : BaseUniformBlock() {
    // Companion
    companion object {
        val instance by lazy(this) { BaseUniformBlock.getImplementation<StablesBlock>() }
    }

    // Attributs
    @Field open var viewMatrix = Mat4.lookAt(PolyRenderer.EYE, PolyRenderer.TARGET, PolyRenderer.UP)
    @Field open var projMatrix = Mat4()
}