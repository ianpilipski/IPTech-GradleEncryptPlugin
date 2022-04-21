package com.iptech.gradle.encrypt

import com.iptech.gradle.encrypt.api.EncryptedFilesSpec
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.tasks.util.PatternSet

@CompileStatic
class DefaultEncryptedFilesSpec extends PatternSet implements EncryptedFilesSpec {
    protected Project project
    protected Object from
    protected String password
    protected Boolean deleteOnClose

    DefaultEncryptedFilesSpec(Project project) {
        this.project = project
        deleteOnClose = null
    }

    void from(Object value) {
        this.from = value
    }

    void setFrom(Object value) {
        this.from = value
    }

    File getFrom() {
        return project.file(this.from)
    }

    void password(String value) {
        this.password = value
    }

    void setPassword(String value) {
        this.password = value
    }

    String getPassword() {
        return this.password
    }

    void deleteOnClose(Boolean value) {
        this.deleteOnClose = value
    }

    void setDeleteOnClose(Boolean value) {
        this.deleteOnClose = value
    }

    Boolean getDeleteOnClose() {
        if(this.deleteOnClose == null) return true
        return this.deleteOnClose
    }
}