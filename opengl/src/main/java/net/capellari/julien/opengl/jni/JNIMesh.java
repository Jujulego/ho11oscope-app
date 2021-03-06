package net.capellari.julien.opengl.jni;

import net.capellari.julien.opengl.Material;
import net.capellari.julien.opengl.Vec2;
import net.capellari.julien.opengl.Vec3;
import net.capellari.julien.opengl.base.JNIClass;

public class JNIMesh extends JNIClass {
    // Constructeurs
    JNIMesh(long handle) {
        super(handle);
    }

    // Méthodes
    public native Material getMaterial();
    public native int[] getIndices();
    public native Vec3[] getVertices();
    public native Vec3[] getNormals();
    public native Vec2[] getTexCoords();
}
