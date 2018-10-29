package net.capellari.julien.ho11oscope.poly.opengl

import android.content.Context
import android.opengl.GLES20
import android.util.Log

class Shaders(context: Context) {
    // Attributs
    private var program: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var mMatrixHandle: Int = 0
    private var vMatrixHandle: Int = 0
    private var lightHandle: Int = 0
    private var lightPowerHandle: Int = 0
    private var positionHandle: Int = 0
    private var normalHandle: Int = 0
    private var ambientColorHandle: Int = 0
    private var diffuseColorHandle: Int = 0
    private var specularColorHandle: Int = 0
    private var specularExpHandle: Int = 0
    private var opacityHandle: Int = 0

    // Constructeur
    init {
        init(context)
    }

    // Méthodes
    private fun init(context: Context) {
        // load shaders
        val vertexShader = GLUtils.loadShader(GLES20.GL_VERTEX_SHADER, "shaders/vertex.glsl", context)
        val fragmentShader = GLUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, "shaders/fragment.glsl", context)

        // compile and link
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        GLUtils.checkGlError("link program")

        Log.d("Shaders", "program linked")

        // get handles
        positionHandle      = GLES20.glGetAttribLocation(program, "aPosition")
        normalHandle        = GLES20.glGetAttribLocation(program, "aNormal")
        ambientColorHandle  = GLES20.glGetAttribLocation(program, "aAmbientColor")
        diffuseColorHandle  = GLES20.glGetAttribLocation(program, "aDiffuseColor")
        specularColorHandle = GLES20.glGetAttribLocation(program, "aSpecularColor")
        specularExpHandle   = GLES20.glGetAttribLocation(program, "aSpecularExp")
        opacityHandle       = GLES20.glGetAttribLocation(program, "aOpacity")
        mvpMatrixHandle  = GLES20.glGetUniformLocation(program, "uMVP")
        mMatrixHandle    = GLES20.glGetUniformLocation(program, "uM")
        vMatrixHandle    = GLES20.glGetUniformLocation(program, "uV")
        lightHandle      = GLES20.glGetUniformLocation(program, "uLight")
        lightPowerHandle = GLES20.glGetUniformLocation(program, "uLightPower")

        // Checks
        Log.w("Shaders", if (positionHandle < 0)      "aPosition : not found"      else "aPosition : $positionHandle")
        Log.w("Shaders", if (normalHandle < 0)        "aNormal : not found"        else "aNormal : $normalHandle")
        Log.w("Shaders", if (ambientColorHandle < 0)  "aAmbientColor : not found"  else "aAmbientColor : $ambientColorHandle")
        Log.w("Shaders", if (diffuseColorHandle < 0)  "aDiffuseColor : not found"  else "aDiffuseColor : $diffuseColorHandle")
        Log.w("Shaders", if (specularColorHandle < 0) "aSpecularColor : not found" else "aSpecularColor : $specularColorHandle")
        Log.w("Shaders", if (specularExpHandle < 0)   "aSpecularExp : not found"   else "aSpecularExp : $specularExpHandle")
        Log.w("Shaders", if (opacityHandle < 0)       "aOpacity : not found"   else "aOpacity : $opacityHandle")
        Log.w("Shaders", if (mvpMatrixHandle < 0)  "uMVP : not found"        else "uMVP : $mvpMatrixHandle")
        Log.w("Shaders", if (mMatrixHandle < 0)    "uM : not found"          else "uM : $mMatrixHandle")
        Log.w("Shaders", if (vMatrixHandle < 0)    "uV : not found"          else "uV : $vMatrixHandle")
        Log.w("Shaders", if (lightHandle < 0)      "uLight : not found"      else "uLight : $lightHandle")
        Log.w("Shaders", if (lightPowerHandle < 0) "uLightPower : not found" else "uLightPower : $lightPowerHandle")
        Log.d("Shaders", "All handles gathered")
    }

    fun render(mvpMatrix: FloatArray, mMatrix: FloatArray, vMatrix: FloatArray, numIndices: Int, ibo: Int, vbo: GLUtils.VBO) {
        GLES20.glUseProgram(program)

        // feed positions, normals and diffuseColors to shader from VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo.id)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glEnableVertexAttribArray(ambientColorHandle)
        GLES20.glEnableVertexAttribArray(diffuseColorHandle)
        GLES20.glEnableVertexAttribArray(specularColorHandle)
        GLES20.glEnableVertexAttribArray(specularExpHandle)
        GLES20.glEnableVertexAttribArray(opacityHandle)

        GLES20.glVertexAttribPointer(positionHandle, GLUtils.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vbo.vOffset)
        GLES20.glVertexAttribPointer(normalHandle,   GLUtils.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, vbo.nOffset)
        GLES20.glVertexAttribPointer(ambientColorHandle,  GLUtils.NUM_COLOR_COMPONENTS, GLES20.GL_FLOAT, false, 0, vbo.caOffset)
        GLES20.glVertexAttribPointer(diffuseColorHandle,  GLUtils.NUM_COLOR_COMPONENTS, GLES20.GL_FLOAT, false, 0, vbo.cdOffset)
        GLES20.glVertexAttribPointer(specularColorHandle, GLUtils.NUM_COLOR_COMPONENTS, GLES20.GL_FLOAT, false, 0, vbo.csOffset)
        GLES20.glVertexAttribPointer(specularExpHandle, 1, GLES20.GL_FLOAT, false, 0, vbo.seOffset)
        GLES20.glVertexAttribPointer(opacityHandle,     1, GLES20.GL_FLOAT, false, 0, vbo.oOffset)
        GLUtils.checkGlError("attributePointers")

        // feed Matrices to shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMatrix, 0)
        GLES20.glUniformMatrix4fv(vMatrixHandle, 1, false, vMatrix, 0)
        GLUtils.checkGlError("uniformMatrices")

        // Light
        GLES20.glUniform3f(lightHandle, 0f, 2f, 25f)
        GLES20.glUniform1f(lightPowerHandle, 600f)
        GLUtils.checkGlError("uniformLight")

        // Bind IBO and render
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_INT, 0)
        GLUtils.checkGlError("drawElements")

        // Clean up
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
        GLES20.glDisableVertexAttribArray(ambientColorHandle)
        GLES20.glDisableVertexAttribArray(diffuseColorHandle)
        GLES20.glDisableVertexAttribArray(specularColorHandle)
        GLES20.glDisableVertexAttribArray(specularExpHandle)
        GLES20.glDisableVertexAttribArray(opacityHandle)
        GLES20.glUseProgram(0)
    }
}