package net.capellari.julien.ho11oscope.poly

import net.capellari.julien.opengl.*
import net.capellari.julien.opengl.base.SharedStorageBlock
import net.capellari.julien.opengl.base.UniformBlock

@UniformBlock
abstract class MatricesBlock : UniformBlock() {
    // Companion
    companion object {
        val instance by lazy(this) { UniformBlock.getImplementation<MatricesBlock>() }
    }

    // Attributs
    @Field open var mvpMatrix   = Mat4()
    @Field open var modelMatrix = Mat4()
    @Field open var lightMatrix = Mat4.identity()
}

@UniformBlock
abstract class StablesBlock : UniformBlock() {
    // Companion
    companion object {
        val instance by lazy(this) { UniformBlock.getImplementation<StablesBlock>() }
    }

    // Attributs
    @Field open var viewMatrix    = Mat4.lookAt(PolyRenderer.EYE, PolyRenderer.TARGET, PolyRenderer.UP)
    @Field open var projMatrix    = Mat4()
    @Field open var lightPosition = Vec3(10f, 1f, -10f)
}

@UniformBlock
abstract class ParametersBlock : UniformBlock() {
    // Companion
    companion object {
        val instance by lazy(this) { UniformBlock.getImplementation<ParametersBlock>() }
    }

    // Attributs
    @Field open var lightPower     = 50f
    @Field open var ambientFactor  = .1f
    @Field open var diffuseFactor  = .7f
    @Field open var specularFactor = .5f
}

@ShaderStorage
abstract class LightsBlock : SharedStorageBlock() {
    // Companion
    companion object {
        val instance by lazy(this) { SharedStorageBlock.getImplementation<LightsBlock>() }
    }

    // Attributs
    @Field open var lights = arrayListOf<Light>()
}