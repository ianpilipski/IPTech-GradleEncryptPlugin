package com.iptech.gradle.encrypt.api

import groovy.transform.CompileStatic
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.util.PatternFilterable

@CompileStatic
interface EncryptedFilesSpec extends PatternFilterable {
    @Input DirectoryProperty getFrom()
    @Input @Optional Property<String> getPassword()
    @Input @Optional Property<Boolean> getDeleteOnClose()
}