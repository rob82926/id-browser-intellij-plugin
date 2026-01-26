package com.github.rob82926.idbrowserintellijplugin.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.*
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.github.rob82926.idbrowserintellijplugin.services.MyProjectService
import java.awt.BorderLayout
import javax.swing.*
import com.intellij.ide.BrowserUtil
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLifeSpanHandlerAdapter
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.network.CefRequest

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val browser = JBCefBrowser()
        installExternalLinkHandlers(browser)
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

    /**
     * Opens external navigation in the OS default browser:
     * - normal clicks / navigations (onBeforeBrowse)
     * - target="_blank" / window.open popups (onBeforePopup)
     */
    private fun installExternalLinkHandlers(jbCefBrowser: JBCefBrowser) {
        jbCefBrowser.jbCefClient.addRequestHandler(object : CefRequestHandlerAdapter() {
            override fun onBeforeBrowse(
                browser: CefBrowser,
                frame: CefFrame,
                request: CefRequest,
                user_gesture: Boolean,
                is_redirect: Boolean
            ): Boolean {
                val url = request.url ?: return false

                val isExternal = !url.startsWith("file://")
                if (isExternal) {
                    BrowserUtil.browse(url)
                    return true // cancel navigation in JCEF
                }

                return false
            }
        }, jbCefBrowser.cefBrowser)

        jbCefBrowser.jbCefClient.addLifeSpanHandler(object : CefLifeSpanHandlerAdapter() {
            override fun onBeforePopup(
                browser: CefBrowser,
                frame: CefFrame,
                targetUrl: String,
                targetFrameName: String
            ): Boolean {
                // Typically triggered by target="_blank" or window.open(...)
                val isExternal = !targetUrl.startsWith("file://")
                if (isExternal) {
                    BrowserUtil.browse(targetUrl)
                    return true // cancel popup (don't open new JCEF window)
                }

                return false
            }
        }, jbCefBrowser.cefBrowser)
    }

}
