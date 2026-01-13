package com.github.rob82926.idbrowserintellijplugin.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.*
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.github.rob82926.idbrowserintellijplugin.services.MyProjectService
import java.awt.BorderLayout
import javax.swing.*


class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val browser = JBCefBrowser()
        val panel = JPanel(BorderLayout())
        panel.add(browser.component, BorderLayout.CENTER)

        val content = ContentFactory.getInstance()
            .createContent(panel, "", false)

        toolWindow.contentManager.addContent(content)

        MyProjectService.getInstance(project).browser = browser
        MyProjectService
            .getInstance(project)
            .load("")
    }

    override fun shouldBeAvailable(project: Project) = true
}
