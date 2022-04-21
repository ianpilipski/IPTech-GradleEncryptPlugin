package com.iptech.gradle.encrypt.internal

import groovy.transform.CompileStatic

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.security.spec.KeySpec

@CompileStatic
class CryptoHandler {
    // The number of times that the password is hashed during the derivation of the symmetric key
    private static final int PBKDF2_ITERATION_COUNT = 300_000;
    private static final int PBKDF2_SALT_LENGTH = 16; //128 bits
    private static final int AES_KEY_LENGTH = 256; //in bits
    // An initialization vector size
    private static final int GCM_NONCE_LENGTH = 12; //96 bits
    // An authentication tag size
    private static final int GCM_TAG_LENGTH = 128; //in bits

    SecretKeyFactory factory
    SecureRandom secureRandom

    CryptoHandler() {
        factory = SecretKeyFactory.getInstance('PBKDF2WithHmacSHA512')
        secureRandom = SecureRandom.getInstanceStrong()
    }

    String encrypt(String input, String password) {
        return encrypt(input.getBytes('UTF-8'), password).encodeBase64().toString()
    }

    String decrypt(String input, String password) {
        return new String(decrypt(input.decodeBase64(), password), 'UTF-8')
    }

    void encryptFile(File srcFile, File destFile, String password) {
        destFile.bytes = encrypt(srcFile.bytes, password)
    }

    void decryptFile(File srcFile, File destFile, String password) {
        destFile.bytes = decrypt(srcFile.bytes, password)
    }

    byte[] encrypt(byte[] input, String password) {
        try {
            // Derive the key, given password and salt
            // A salt is a unique, randomly generated string
            // that is added to each password as part of the hashing process
            byte[] salt = new byte[PBKDF2_SALT_LENGTH]
            //TODO: this locks up in a docker container unless you mount the host -v /dev/urandom:/dev/random
            secureRandom.nextBytes(salt)
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATION_COUNT, AES_KEY_LENGTH)
            byte[] secret = factory.generateSecret(keySpec).getEncoded()
            SecretKey key = new SecretKeySpec(secret, "AES")

            // AES-GCM encryption
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding")
            // A nonce or an initialization vector is a random value chosen at encryption time
            // and meant to be used only once
            byte[] nonce = new byte[GCM_NONCE_LENGTH]
            secureRandom.nextBytes(nonce)
            // An authentication tag
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec)
            byte[] encrypted = cipher.doFinal(input)
            // Salt and nonce can be stored together with the encrypted data
            // Both salt and nonce have fixed length, so can be prefixed to the encrypted data
            ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + nonce.length + encrypted.length)
            byteBuffer.put(salt)
            byteBuffer.put(nonce)
            byteBuffer.put(encrypted)
            return byteBuffer.array()
        } catch (Exception e) {
            throw new RuntimeException(e)
        }
    }

    byte[] decrypt(byte[] encrypted, String password) {
        try {
            // Salt and nonce have to be extracted
            ByteBuffer byteBuffer = ByteBuffer.wrap(encrypted)
            byte[] salt = new byte[PBKDF2_SALT_LENGTH]
            byteBuffer.get(salt)
            byte[] nonce = new byte[GCM_NONCE_LENGTH]
            byteBuffer.get(nonce)
            byte[] cipherBytes = new byte[byteBuffer.remaining()]
            byteBuffer.get(cipherBytes)

            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATION_COUNT, AES_KEY_LENGTH);
            byte[] secret = factory.generateSecret(keySpec).getEncoded()
            SecretKey key = new SecretKeySpec(secret, "AES")

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding")
            // If encrypted data is altered, during decryption authentication tag verification will fail
            // resulting in AEADBadTagException
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec)

            return cipher.doFinal(cipherBytes)
        } catch (Exception e) {
            throw new RuntimeException('failed to decrypt', e)
        }
    }
}
