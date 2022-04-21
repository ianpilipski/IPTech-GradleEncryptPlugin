package com.iptech.gradle.encrypt.tasks


import com.iptech.gradle.encrypt.api.EncryptedFilesSpec
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option

@CompileStatic
abstract class DecryptFilesTask extends EncryptPluginDefaultTask {
    private Set<EncryptedFilesSpec> encryptedFilesSpecs
    private Set<File> outputFilesSet
    private Boolean keepAll

    @Input @Optional abstract Property<Boolean> getDeleteOnClose()
    @Input @Optional abstract Property<String> getPassword()

    @InputFiles Iterable<File> getInputFiles() { context.resolver.getFiles(encryptedFilesSpecs, false) }
    @OutputFiles Iterable<File> getOutputFiles() { outputFilesSet  }

    DecryptFilesTask() {
        outputFilesSet = []
        encryptedFilesSpecs = []

        outputs.upToDateWhen {
            boolean retVal = true
            inputFiles.each {File f ->
                File outFile = context.resolver.getOutputFile(f, false)
                if(!outFile.exists()) {
                    retVal = false
                }
            }
            return retVal
        }
    }

    void encryptedFiles(Action<? extends EncryptedFilesSpec> action) {
        encryptedFiles(context.encryptedFilesSpecFromAction(action))
    }

    void encryptedFiles(Iterable<EncryptedFilesSpec> value) {
        encryptedFilesSpecs.addAll(value)
    }

    void encryptedFiles(EncryptedFilesSpec value) {
        encryptedFilesSpecs.add(value)
    }

    @Option(option = "keepDecrypted", description = "Does not delete the decrypted files after running decrypt")
    private void setKeepDecrypted() {
        keepAll = true
        deleteOnClose.set(false)
    }

    @TaskAction
    void execute() {
        String taskPass = password.getOrNull()
        boolean taskDelete = deleteOnClose.getOrElse(true)
        encryptedFilesSpecs.each { EncryptedFilesSpec it ->
            String pass = it.password ?: taskPass
            boolean del = it.deleteOnClose
            if(keepAll) del = false
            outputFilesSet.addAll(context.executor.decryptFiles(context.resolver.getFiles(it, false), pass, del))
        }
    }
}