package net.capellari.julien.opengl.jni;

import net.capellari.julien.opengl.Vec3;
import net.capellari.julien.opengl.base.JNIClass;

public class JNIMesh extends JNIClass {
    // Constructeurs
    JNIMesh(long handle) {
        super(handle);
    }

    // Méthodes
    public native Vec3[] getVertices();
    public native Vec3[] getNormals();
    public native int[] getIndices();
}
