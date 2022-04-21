package com.iptech.gradle.encrypt.internal

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Paths

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CryptoExecutorTest extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'com.iptech.gradle.encrypt-plugin'
            }
        """

        def encryptedFile = testProjectDir.newFile('testfile.encrypted')
        encryptedFile.bytes = new CryptoHandler().encrypt('dood'.bytes, 'hello')
    }

    void 'test copy'() {
        buildFile << """
            def f = project.file('testfile.encrypted')
            def spec = project.copySpec {
                from f.parent
                include f.name
                into 'dood'
                rename { String fn -> fn + ".myext" }
            }
            task runMyTest(type: Copy) {
                with spec
                into '.'
            }
        """
        when:
        def result = runMyTest()

        then:
        result.task(':runMyTest').outcome == SUCCESS
        Paths.get(testProjectDir.root.path, 'dood', 'testfile.encrypted.myext').toFile().exists()
    }

    void 'decryptFile with direct password'() {
        buildFile << """
            task runMyTest {
                doLast {
                    encrypt.decryptFile('testfile.encrypted', 'hello', false)
                }
            }
        """
        File decryptedFile = new File('testfile', testProjectDir.getRoot())

        when:
        def result = runMyTest()


        then:
        result.task(':runMyTest').outcome == SUCCESS
        decryptedFile.exists()
        decryptedFile.text == 'dood'
    }

    void 'decryptFile with config password'() {
        buildFile << """
            encrypt {
                password = 'hello'
            }
            
            task runMyTest {
                doLast {
                    encrypt.decryptFile('testfile.encrypted', false)
                }
            }
        """
        File decryptedFile = new File('testfile', testProjectDir.getRoot())

        when:
        def result = runMyTest()

        then:
        result.task(':runMyTest').outcome == SUCCESS
        decryptedFile.exists()
        decryptedFile.text == 'dood'
    }

    private BuildResult runMyTest() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('runMyTest', '--stacktrace')
                .withPluginClasspath()
                .forwardOutput()
                .build()
    }
}