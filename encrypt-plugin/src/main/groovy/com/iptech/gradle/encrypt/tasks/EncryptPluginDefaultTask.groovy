package com.iptech.gradle.encrypt.tasks

import com.iptech.gradle.encrypt.internal.PluginContext
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal

@CompileStatic
abstract class EncryptPluginDefaultTask extends DefaultTask {
    private static PluginContext _context

    @Internal
    protected PluginContext getContext() {
        if(EncryptPluginDefaultTask._context==null) {
            throw new Exception('CryptoExecutor is null, you are trying to access this member before the plugin has been applied')
        }
        return EncryptPluginDefaultTask._context
    }

    static void initBaseTasks(PluginContext value) {
        this._context = value
    }
}