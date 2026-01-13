package com.github.rob82926.idbrowserintellijplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.*
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.github.rob82926.idbrowserintellijplugin.MyBundle
import com.github.rob82926.idbrowserintellijplugin.services.MyProjectService
import java.awt.BorderLayout
import javax.swing.*


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        //val IdLookupBrowserWindow = MyToolWindow(toolWindow)
        //val content = ContentFactory.getInstance().createContent(IdLookupBrowserWindow.getContent(), null, false)

        val browser = JBCefBrowser()
        val panel = JPanel(BorderLayout())
        panel.add(browser.component, BorderLayout.CENTER)

        val content = ContentFactory.getInstance()
            .createContent(panel, "", false)

        toolWindow.contentManager.addContent(content)

        MyProjectService.getInstance(project).browser = browser
        MyProjectService
            .getInstance(project)
            .load("https://www.google.com/")
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val label = JBLabel(MyBundle.message("randomLabel", "?"))

            add(label)
            add(JButton(MyBundle.message("shuffle")).apply {
                addActionListener {
                    label.text = MyBundle.message("randomLabel", service.getRandomNumber())
                }
            })
        }
    }
}
