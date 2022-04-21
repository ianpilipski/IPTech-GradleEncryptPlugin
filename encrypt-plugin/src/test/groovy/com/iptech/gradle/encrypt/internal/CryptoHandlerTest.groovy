package com.iptech.gradle.encrypt.internal

import spock.lang.Specification

class CryptoHandlerTest extends Specification {
    final static String expectedString = 'thisIsATest!@#$%^&*()_01234567890'

    void string_is_encrypted_decrypted_properly() {
        when:
        CryptoHandler systemUnderTest = new CryptoHandler()
        String encryptedString = systemUnderTest.encrypt(expectedString, 'testPass')
        String decryptedString = systemUnderTest.decrypt(encryptedString, 'testPass')

        then:
        expectedString != encryptedString
        expectedString == decryptedString
    }

    void string_is_not_decrypted_with_wrong_password() {
        when:
        CryptoHandler systemUnderTest = new CryptoHandler()
        String encryptedString = systemUnderTest.encrypt(expectedString, 'testPass')

        boolean failed = false
        try {
            String decryptedString = systemUnderTest.decrypt(encryptedString, 'wrongPass')
        } catch(RuntimeException e) {
            failed = true
        }

        then:
        expectedString != encryptedString
        failed == true
    }
}
