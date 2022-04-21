package com.iptech.gradle.encrypt

import com.iptech.gradle.encrypt.api.EncryptedFilesSpec
import groovy.transform.CompileStatic
import org.gradle.api.tasks.util.PatternSet

@CompileStatic
abstract class DefaultEncryptedFilesSpec extends PatternSet implements EncryptedFilesSpec {

}