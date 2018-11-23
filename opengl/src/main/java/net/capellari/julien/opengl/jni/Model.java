package net.capellari.julien.opengl.jni;

import net.capellari.julien.opengl.Material;
import net.capellari.julien.opengl.base.JNIClass;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;

public class Model extends JNIClass {
    // Méthodes statiques
    private static native long construct(@NonNull String file);

    // Constructeur
    public Model(@NonNull String file) {
        super(construct(file));
    }

    // Méthodes
    public native HashMap<String,Material> getMaterials();

    private native long[] nativeMeshes();
    public ArrayList<JNIMesh> getMeshes() {
        ArrayList<JNIMesh> m = new ArrayList<>();

        for (long h : nativeMeshes()) {
            m.add(new JNIMesh(h));
        }

        return m;
    }
}
