package com.github.rob82926.idbrowserintellijplugin.settings

import com.intellij.openapi.components.*

@Service(Service.Level.APP) // Explicitly set to APP level
@State(
    name = "com.github.rob82926.idbrowserintellijplugin.settings.ReqSettingsState",
    storages = [Storage("id-browser.xml")]
)
class ReqSettingsState : PersistentStateComponent<ReqSettingsState> {
    var regex: String = "REQ\\d+"
    var urlTemplate: String = "http://www.google.com/?q=%s"

    override fun getState(): ReqSettingsState = this

    override fun loadState(state: ReqSettingsState) {
        regex = state.regex
        urlTemplate = state.urlTemplate
    }

    companion object {
        fun getInstance(): ReqSettingsState = service<ReqSettingsState>()
    }
}
