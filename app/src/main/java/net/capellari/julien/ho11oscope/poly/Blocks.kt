package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.Field
import net.capellari.julien.opengl.Mat4
import net.capellari.julien.opengl.UniformBlock
import net.capellari.julien.opengl.Vec3
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
}

@UniformBlock
abstract class StablesBlock : BaseUniformBlock() {
    // Companion
    companion object {
        val instance by lazy(this) { BaseUniformBlock.getImplementation<StablesBlock>() }
    }

    // Attributs
    @Field open var viewMatrix    = Mat4.lookAt(PolyRenderer.EYE, PolyRenderer.TARGET, PolyRenderer.UP)
    @Field open var projMatrix    = Mat4()
    @Field open var lightPosition = Vec3(10f, 0f, -10f)
}

@UniformBlock
abstract class ParametersBlock : BaseUniformBlock() {
    // Companion
    companion object {
        val instance by lazy(this) { BaseUniformBlock.getImplementation<ParametersBlock>() }
    }

    // Attributs
    @Field open var lightPower     = 50f
    @Field open var ambientFactor  = .1f
    @Field open var diffuseFactor  = .7f
    @Field open var specularFactor = .5f
}