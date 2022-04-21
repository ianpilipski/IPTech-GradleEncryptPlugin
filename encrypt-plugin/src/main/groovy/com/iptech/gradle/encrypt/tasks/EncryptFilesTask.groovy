package com.iptech.gradle.encrypt.tasks

import com.iptech.gradle.encrypt.DefaultEncryptedFilesSpec
import com.iptech.gradle.encrypt.api.EncryptedFilesSpec
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

@CompileStatic
abstract class EncryptFilesTask extends EncryptPluginDefaultTask {
    private Set<EncryptedFilesSpec> encryptedFilesSpecs
    private Set<File> outputFilesSet

    @Input @Optional abstract Property<String> getPassword()

    @InputFiles Iterable<File> getInputFiles() { context.resolver.getFiles(encryptedFilesSpecs, true) }
    @OutputFiles Iterable<File> getOutputFiles() { outputFilesSet  }

    EncryptFilesTask() {
        outputFilesSet = []
        encryptedFilesSpecs = []
    }

    void encryptedFiles(Action<? extends EncryptedFilesSpec> action) {
        EncryptedFilesSpec spec = context.objectFactory.newInstance(DefaultEncryptedFilesSpec.class)
        action.execute(spec)
        encryptedFiles(spec)
    }

    void encryptedFiles(Iterable<EncryptedFilesSpec> value) {
        encryptedFilesSpecs.addAll(value)
    }

    void encryptedFiles(EncryptedFilesSpec value) {
        encryptedFilesSpecs.add(value)
    }

    @TaskAction
    void execute() {
        String taskPass = password.getOrNull()
        encryptedFilesSpecs.each { EncryptedFilesSpec it ->
            String pass = it.password.getOrElse(taskPass)
            outputFilesSet.addAll(context.executor.encryptFiles(context.resolver.getFiles(it, true), pass))
        }
    }
}