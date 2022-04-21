package com.iptech.gradle.encrypt.internal

import groovy.transform.CompileStatic

@CompileStatic
class CryptoExecutor {
    static final List<File> cleanupFiles = []

    CryptoHandler cryptoHandler
    SpecResolver resolver

    CryptoExecutor(SpecResolver resolver) {
        this.cryptoHandler = new CryptoHandler()
        this.resolver = resolver

        Runtime.getRuntime().addShutdownHook(new Thread() {
            void run() {
                deleteDecryptedFiles()
            }
        })
    }

    void deleteDecryptedFiles() {
        cleanupFiles.each {
            try {
                if(it.exists()) {
                    it.delete()
                }
            } catch(Exception) {}
        }
        cleanupFiles.clear()
    }

    String decryptString(String value, String password) { cryptoHandler.decrypt(value, password) }
    String encryptString(String value, String password) { cryptoHandler.encrypt(value, password) }

    File encryptFile(File file, String password) {
        return performEncryptionOperationOnFiles([file], password, false, true, true)[0]
    }

    Iterable<File> encryptFiles(Iterable<File> files, String password) {
        return performEncryptionOperationOnFiles(files, password, false, false, true)
    }

    File decryptFile(File file, String password, boolean deleteOnClose=true) {
        return performEncryptionOperationOnFiles([file], password, deleteOnClose, true, false)[0]
    }

    Iterable<File> decryptFiles(Iterable<File> files, String password, boolean deleteOnClose) {
        return performEncryptionOperationOnFiles(files, password, deleteOnClose, false, false)
    }

    private Iterable<File> performEncryptionOperationOnFiles(Iterable<File> files, String password, boolean deleteOnClose, boolean singleFileOnly, boolean encrypt) {
        final List<File> outputFiles = []
        final Iterable<File> inputFiles = files

        if(singleFileOnly && inputFiles.size()>1) {
            throw new Exception('asked to decrypt a single file but multiple were found')
        }

        inputFiles.each { File srcFile ->
            File outFile = resolver.getOutputFile(srcFile, encrypt)
            if(encrypt) {
                String encryptedName = "${srcFile.name}.encrypted"
                println "Encrypting: ${srcFile.path} ==> ${encryptedName}"
                outFile.bytes = cryptoHandler.encrypt(srcFile.bytes, password)
                outputFiles.add(outFile)
            } else {
                println "Decrypting ${srcFile.path}"
                outFile.bytes = cryptoHandler.decrypt(srcFile.bytes, password)
                outputFiles.add(outFile)

                if (deleteOnClose) {
                    addFileToCleanup(outFile)
                }
            }
        }

        return outputFiles
    }

    private static void addFileToCleanup(File file) {
        println "Will delete decrypted file ${file.name} files on close. To keep them around use --keepDecrypted on the commandline."
        file.deleteOnExit()
        cleanupFiles.add(file)
    }
}