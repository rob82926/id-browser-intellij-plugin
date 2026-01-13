package com.github.rob82926.idbrowserintellijplugin

import com.github.rob82926.idbrowserintellijplugin.settings.ReqSettingsState
import com.github.rob82926.idbrowserintellijplugin.services.MyProjectService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

class ReqPsiReference(
    element: PsiElement,
    range: IntRange,
    private val value: String
) : PsiReferenceBase<PsiElement>(
    element,
    TextRange(range.first, range.last + 1)
) {

    override fun resolve(): PsiElement? = null

    fun navigate(requestFocus: Boolean) {
        val project = element.project
        val state = ReqSettingsState.getInstance()
        val url = state.urlTemplate.format(value)

        MyProjectService
            .getInstance(project)
            .load(url)
    }
}
