package com.store.data.preference.encrypted

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


interface AES256Util {

    /**
     * AED256알고리즘 사용 및 [plainText]를 암호화한다.
     */
    fun encrypt(plainText: String): String

    /**
     * AED256알고리즘 사용 및 [encryptedText]를 복호화한다.
     */
    fun decrypt(encryptedText: String): String

}

class AES256UtilImpl private constructor(): AES256Util {

    companion object {
        val instance: AES256UtilImpl by lazy { AES256UtilImpl() }
        private const val AES_256_KEY_STORE_ALIAS = "AES_256_KEY_STORE_ALIAS"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val IV_LENGTH = 16
    }

    private lateinit var key: SecretKey
    init { initAndroidKeyStore() }

    @Throws(Exception::class)
    private fun initAndroidKeyStore() {
        if (!isExistKey()) {
            generateKey()
        }
        key = getKey()
    }

    private fun isExistKey(): Boolean {
        val aliases = KeyStore.getInstance(ANDROID_KEY_STORE).run {
            load(null)
            aliases()
        }
        while (aliases.hasMoreElements()) {
            val nextAlias = aliases.nextElement()
            if (nextAlias == AES_256_KEY_STORE_ALIAS) {
                return true
            }
        }
        return false
    }

    private fun generateKey() {
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            AES_256_KEY_STORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getKey(): SecretKey {
        return KeyStore.getInstance(ANDROID_KEY_STORE).let { keyStore ->
            keyStore.load(null)
            keyStore.getEntry(AES_256_KEY_STORE_ALIAS, null) as KeyStore.SecretKeyEntry
        }.secretKey
    }

    private val cipher: Cipher by lazy { Cipher.getInstance("AES/GCM/NoPadding") }

    override fun encrypt(plainText: String): String {
        return cipher.run {
            init(Cipher.ENCRYPT_MODE, key)
            val encText = Base64.encodeToString(doFinal(plainText.toByteArray()), 0)
            val ivText = Base64.encodeToString(iv, 0)
            ivText + encText
        }
    }

    override fun decrypt(encryptedText: String): String {
        return cipher.run {
            val (ivText, encryptedContentsText) =
                encryptedText.substring(0, IV_LENGTH) to encryptedText.substring(IV_LENGTH)
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, Base64.decode(ivText, 0)))
            doFinal(Base64.decode(encryptedContentsText, 0))
        }.let(::String)
    }
}