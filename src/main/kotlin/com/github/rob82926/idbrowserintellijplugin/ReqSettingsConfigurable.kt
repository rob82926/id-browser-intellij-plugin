package com.github.rob82926.idbrowserintellijplugin.settings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager
import java.awt.BorderLayout
import javax.swing.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiManager

class ReqSettingsConfigurable : Configurable {

    private var regexField: JTextField? = null
    private var urlField: JTextField? = null

    override fun createComponent(): JComponent {
        thisLogger().warn("Created settings!")

        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        regexField = JTextField(20)
        urlField = JTextField(20)

        panel.add(JLabel("Regex pattern:"))
        panel.add(regexField)
        panel.add(Box.createVerticalStrut(10))
        panel.add(JLabel("URL template (use %s for match):"))
        panel.add(urlField)

        // Reset the fields to the stored state immediately after creation
        reset()

        val wrapper = JPanel(BorderLayout())
        wrapper.add(panel, BorderLayout.NORTH)
        return wrapper
    }

    override fun isModified(): Boolean {
        val state = ReqSettingsState.getInstance()
        return regexField?.text != state.regex ||
                urlField?.text != state.urlTemplate
    }

    override fun apply() {
        val state = ReqSettingsState.getInstance()
        val oldRegex = state.regex
        val newRegex = regexField?.text ?: ""
        val newUrl = urlField?.text ?: ""

        // Update state
        state.regex = newRegex
        state.urlTemplate = newUrl

        // Trigger refresh if settings changed
        if (oldRegex != newRegex) {
            ApplicationManager.getApplication().invokeLater {
                ProjectManager.getInstance().openProjects.forEach { project ->
                    // 1. Drop the internal PSI caches so the contributor runs again
                    PsiManager.getInstance(project).dropPsiCaches()
                    // 2. Restart highlighting
                    DaemonCodeAnalyzer.getInstance(project).restart()
                }
            }
        }
    }

    override fun reset() {
        val state = ReqSettingsState.getInstance()
        regexField?.text = state.regex
        urlField?.text = state.urlTemplate
    }

    override fun getDisplayName() = "ID Lookup Browser"
}
