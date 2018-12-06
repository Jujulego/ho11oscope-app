package net.capellari.julien.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.google.common.io.BaseEncoding
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

// Extentions
fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun Context.getSHA1Cert() : String? {
    try {
        val signatures: Array<Signature>

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            signatures = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo.signingCertificateHistory
        } else {
            @Suppress("DEPRECATION")
            signatures = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
        }

        for (signature in signatures) {
            val md = MessageDigest.getInstance("SHA-1")
            md.update(signature.toByteArray())
            return BaseEncoding.base16().encode(md.digest())
        }
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("Context", "Error while getting SHA1 signature : ", e)
    } catch (e: NoSuchAlgorithmException) {
        Log.e("Context", "No SHA1 signature :", e)
    }

    return null
}

inline val <T : Any> KProperty0<T>.sharedPreference: String?
    get() {
        isAccessible = true
        val delegate = getDelegate()
        isAccessible = false

        return if (delegate is BaseSharedPreference<*,*>) delegate.name else null
    }