package com.github.rob82926.idbrowserintellijplugin.toolWindow

import com.github.rob82926.idbrowserintellijplugin.settings.ReqSettingsState
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
import java.net.URI

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

                val openInternal = shouldOpenInternally(url)
                if (!openInternal) {
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
                val openInternal = shouldOpenInternally(targetUrl)
                if (!openInternal) {
                    BrowserUtil.browse(targetUrl)
                    return true // cancel popup (don't open new JCEF window)
                }

                return false
            }
        }, jbCefBrowser.cefBrowser)
    }

    private fun shouldOpenInternally(url: String): Boolean {
        val template = ReqSettingsState.getInstance().urlTemplate.trim()
        if (template.isEmpty()) return false

        // Require a placeholder so the "internal site" can be derived deterministically
        if (!template.contains("%s")) return false

        // Build a stable "prefix" from the template (everything before %s)
        val marker = "__ID_BROWSER_PLACEHOLDER__"
        val expanded = template.replace("%s", marker)

        val prefix = expanded.substringBefore(marker)
        if (prefix.isBlank()) return false

        // Quick wins (handles query-based templates like https://host/path?q=%s)
        if (url.startsWith(prefix)) return true

        // If prefixes don't match exactly, try a slightly more permissive check based on origin.
        // This helps when minor formatting differences exist, but still keeps you on the same site.
        return try {
            val t = URI(expanded)
            val u = URI(url)

            val sameScheme = t.scheme.equals(u.scheme, ignoreCase = true)
            val sameHost = t.host.equals(u.host, ignoreCase = true)
            val samePort = (t.port.takeIf { it != -1 } ?: defaultPort(t.scheme)) ==
                    (u.port.takeIf { it != -1 } ?: defaultPort(u.scheme))

            sameScheme && sameHost && samePort && url.startsWith(prefix.substringBefore('?'))
        } catch (_: Exception) {
            false
        }
    }

    private fun defaultPort(scheme: String?): Int =
        when (scheme?.lowercase()) {
            "http" -> 80
            "https" -> 443
            else -> -1
        }
}
