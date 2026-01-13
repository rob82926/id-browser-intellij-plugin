package com.github.rob82926.idbrowserintellijplugin.settings

import com.intellij.openapi.options.Configurable
import java.awt.BorderLayout
import javax.swing.*

class ReqSettingsConfigurable : Configurable {

    private var regexField: JTextField? = null
    private var urlField: JTextField? = null

    override fun createComponent(): JComponent {
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
        state.regex = regexField?.text ?: ""
        state.urlTemplate = urlField?.text ?: ""
    }

    override fun reset() {
        val state = ReqSettingsState.getInstance()
        regexField?.text = state.regex
        urlField?.text = state.urlTemplate
    }

    override fun getDisplayName() = "ID Lookup Browser"
}
