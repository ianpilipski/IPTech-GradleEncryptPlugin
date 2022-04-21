package com.iptech.gradle.encrypt


import com.iptech.gradle.encrypt.internal.PluginContext
import com.iptech.gradle.encrypt.tasks.DecryptFilesTask
import com.iptech.gradle.encrypt.tasks.DecryptStringTask
import com.iptech.gradle.encrypt.tasks.EncryptFilesTask
import com.iptech.gradle.encrypt.tasks.EncryptPluginDefaultTask
import com.iptech.gradle.encrypt.tasks.EncryptStringTask
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class EncryptPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def pc = new PluginContext(project)
        EncryptPluginDefaultTask.initBaseTasks(pc)

        EncryptExtension encryptExtension = project.extensions.create('encrypt', EncryptExtension, pc)

        establishConventions(project, encryptExtension)
        project.afterEvaluate {
            createTasks(project, encryptExtension)
        }

        project.gradle.buildFinished {
            pc.executor.deleteDecryptedFiles()
        }
    }

    private void establishConventions(Project project, EncryptExtension encryptExtension) {
        project.tasks.withType(EncryptStringTask.class).configureEach { EncryptStringTask it ->
            it.password.convention(encryptExtension.password)
        }

        project.tasks.withType(DecryptStringTask.class).configureEach { DecryptStringTask it ->
            it.password.convention(encryptExtension.password)
        }

        project.tasks.withType(DecryptFilesTask).configureEach { DecryptFilesTask it ->
            it.password.convention(encryptExtension.password)
            it.deleteOnClose.convention(true)
        }

        project.tasks.withType(EncryptFilesTask).configureEach { EncryptFilesTask it ->
            it.password.convention(encryptExtension.password)
        }
    }

    private void createTasks(Project project, EncryptExtension encryptExtension) {
        project.tasks.create('encryptAll', EncryptFilesTask) {EncryptFilesTask it ->
            it.group = 'encrypt'
            it.description = 'encrypt all un-encrypted files in the encryptedFiles configuration'
            it.encryptedFiles(encryptExtension.encryptedFiles)
        }

        project.tasks.create('decryptAll', DecryptFilesTask) { DecryptFilesTask it ->
            it.group = 'encrypt'
            it.description = 'decrypt all encrypted files in the encryptedFiles configuration'
            it.encryptedFiles(encryptExtension.encryptedFiles)
        }

        project.tasks.create('encryptString', EncryptStringTask) { EncryptStringTask it ->
            it.group = 'encrypt'
            it.description = 'encrypt a string using --value and optional --password on the commandline'
        }

        project.tasks.create('decryptString', DecryptStringTask) { DecryptStringTask it ->
            it.group = 'encrypt'
            it.description = 'decrypt a string using --value and optional --pasword on the commandline'
        }
    }
}
