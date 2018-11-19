package net.capellari.julien.opengl;

import net.capellari.julien.opengl.base.JNIClass;

public class JNITest extends JNIClass {
    // Méthodes statiques
    private static native long construct();

    // Constrcuteur
    public JNITest() {
        super(construct());
    }

    // Méthodes
    public native int test();
}
