package net.capellari.julien.opengl.base;

public abstract class JNIClass {
    static {
        System.loadLibrary("opengl");
    }

    // Attributs
    @SuppressWarnings("unused,FieldCanBeLocal")
    private final long nativeHandle;

    // Constructeurs
    protected JNIClass(long handle) {
        nativeHandle = handle;
    }

    // Méthodes
    public native void dispose();
}
