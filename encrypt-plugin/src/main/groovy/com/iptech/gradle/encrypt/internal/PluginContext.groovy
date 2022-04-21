package com.iptech.gradle.encrypt.internal

import com.iptech.gradle.encrypt.DefaultEncryptedFilesSpec
import com.iptech.gradle.encrypt.api.EncryptedFilesSpec
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.reflect.ObjectInstantiationException

@CompileStatic
class PluginContext {
    final Project project
    final CryptoExecutor executor
    final SpecResolver resolver

    PluginContext(Project project) {
        this.project = project
        this.resolver = new SpecResolver(project)
        this.executor = new CryptoExecutor(resolver)
    }

    public <T> T newInstance(Class<? extends T> aClass, Object... objects) throws ObjectInstantiationException {
        project.objects.newInstance(aClass, objects)
    }

    ObjectFactory getObjectFactory() {
        project.objects
    }

    DefaultEncryptedFilesSpec encryptedFilesSpecFromAction(Action<? extends EncryptedFilesSpec> action) {
        DefaultEncryptedFilesSpec spec = new DefaultEncryptedFilesSpec(project)
        action.execute(spec)
        return spec
    }
}