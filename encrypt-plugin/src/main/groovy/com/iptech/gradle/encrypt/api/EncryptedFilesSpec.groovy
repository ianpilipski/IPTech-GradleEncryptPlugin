package com.iptech.gradle.encrypt.api

import groovy.transform.CompileStatic
import org.gradle.api.provider.Property
import org.gradle.api.tasks.util.PatternFilterable

@CompileStatic
interface EncryptedFilesSpec extends PatternFilterable {
    void from(Object value)
    void setFrom(Object value)
    File getFrom()

    void password(String value)
    void setPassword(String value)
    String getPassword()

    void deleteOnClose(Boolean value)
    void setDeleteOnClose(Boolean value)
    Boolean getDeleteOnClose()
}