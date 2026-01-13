package com.github.rob82926.idbrowserintellijplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser

@Service(Service.Level.PROJECT)
class MyProjectService(project: Project) {
    lateinit var browser: JBCefBrowser

    fun load(url: String) {
        if (::browser.isInitialized) {
            browser.loadURL(url)
        }
    }

    companion object {
        fun getInstance(project: Project): MyProjectService =
            project.getService(MyProjectService::class.java)
    }
}
