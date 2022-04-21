package com.iptech.gradle.encrypt

import com.iptech.gradle.encrypt.api.EncryptedFilesSpec
import com.iptech.gradle.encrypt.internal.PluginContext
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import java.util.concurrent.Callable

@CompileStatic
abstract class EncryptExtension {
    protected PluginContext context

    DomainObjectSet<EncryptedFilesSpec> encryptedFiles
    abstract Property<String> getPassword()

    EncryptExtension(PluginContext context) {
        this.context = context
        this.encryptedFiles = context.objectFactory.domainObjectSet(EncryptedFilesSpec.class)
    }

    void encryptedFiles(Action<? extends EncryptedFilesSpec> action) {
        this.encryptedFiles.add(context.encryptedFilesSpecFromAction(action))
    }

    void password(Callable<String> callable) {
        password.set(context.project.provider(callable))
    }

    void password(String value) {
        password.set(value)
    }

    String decryptString(String input) {
        return decryptString(input, password.get())
    }

    String decryptString(String input, String password) {
        return context.executor.decryptString(input, password)
    }

    String encryptString(String input) {
        return encryptString(input, password.get())
    }

    String encryptString(String input, String password) {
        return context.executor.encryptString(input, password)
    }

    Iterable<File> encryptFiles(Action<? extends EncryptedFilesSpec> action) {
        encryptFiles(context.encryptedFilesSpecFromAction(action))
    }

    Iterable<File> encryptFiles(EncryptedFilesSpec spec) {
        context.executor.encryptFiles(context.resolver.getFiles(spec, true), determinePassword(spec.password))
    }

    File decryptFile(Object file) {
        decryptFile(file, password.get(), true)
    }

    File decryptFile(Object file, String password) {
        decryptFile(file, password, true)
    }

    File decryptFile(Object file, boolean deleteOnClose) {
        decryptFile(file, password.get(), deleteOnClose)
    }

    File decryptFile(Object file, String password, boolean deleteOnClose) {
        decryptFile(context.project.file(file), password, deleteOnClose)
    }

    File decryptFile(File file, String password, boolean deleteOnClose) {
        context.executor.decryptFile(file, password, deleteOnClose)
    }

    Iterable<File> decryptFiles(Action<? extends EncryptedFilesSpec> action) {
        decryptFiles(context.encryptedFilesSpecFromAction(action))
    }

    Iterable<File> decryptFiles(EncryptedFilesSpec spec) {
        context.executor.decryptFiles(context.resolver.getFiles(spec, false), determinePassword(spec.password), spec.deleteOnClose)
    }

    private String determinePassword(String value) {
        (value!=null) ? value : password.get()
    }
}
