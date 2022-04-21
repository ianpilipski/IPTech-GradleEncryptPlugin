package com.iptech.gradle.encrypt.internal

import com.iptech.gradle.encrypt.api.EncryptedFilesSpec
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.file.FileVisitDetails

@CompileStatic
class SpecResolver {
    protected Project project

    SpecResolver(Project project) {
        this.project = project
    }

    File getOutputFile(File srcFile, boolean encrypt) {
        if(encrypt) {
            String encryptedName = "${srcFile.name}.encrypted"
            return new File(srcFile.parentFile, encryptedName)
        }
        return new File(srcFile.parentFile, srcFile.name.substring(0, srcFile.name.length() - '.encrypted'.length()))
    }

    Iterable<File> getFiles(EncryptedFilesSpec spec, boolean encrypt) {
        getFiles([spec], encrypt)
    }

    Iterable<File> getFiles(Iterable<EncryptedFilesSpec> specs, boolean encrypt) {
        Set<File> retVal = []
        specs.each { EncryptedFilesSpec spec ->
            project.fileTree(spec.from).matching(spec).visit { FileVisitDetails fvd ->
                if(isFileValid(fvd.file, encrypt)) retVal.add(fvd.file)
            }
        }
        return retVal
    }

    protected boolean isFileValid(File file, boolean encrypt) {
        if (!file.isDirectory()) {
            boolean hasEncryptExt = file.name.endsWith('.encrypted')
            return encrypt ? (!hasEncryptExt) : hasEncryptExt
        }
        return false
    }
}