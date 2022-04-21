package com.iptech.gradle.encrypt.tasks

import com.iptech.gradle.encrypt.internal.CryptoHandler
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

@CompileStatic
abstract class DecryptStringTask extends EncryptPluginDefaultTask {
    @Input abstract Property<String> getValue()
    @Input abstract Property<String> getPassword()

    @Option(option = "password", description = "The commandline password to use for decryption")
    private void setPasswordCommandline(String val) {
        password.set(val)
    }

    @Option(option = "value", description = "The commandline value of the string to decrypt")
    private void setValueCommandline(String val) {
        value.set(val)
    }

    @TaskAction
    void execute() {
        String retVal = context.executor.decryptString(value.get(), password.get())
        println "\nDecrypted String:\n\n" +
                "    String: ${value.get()}\n" +
                "    Result: ${retVal}\n"
    }
}