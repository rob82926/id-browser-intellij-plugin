package com.github.rob82926.idbrowserintellijplugin

import com.github.rob82926.idbrowserintellijplugin.settings.ReqSettingsState
import com.github.rob82926.idbrowserintellijplugin.services.MyProjectService
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.TextRange
import com.intellij.pom.Navigatable
import com.intellij.psi.*
import com.intellij.psi.impl.FakePsiElement
import com.intellij.openapi.wm.ToolWindowManager

class ReqPsiReference(
    element: PsiElement,
    range: IntRange,
    private val value: String
) : PsiReferenceBase<PsiElement>(
    element,
    TextRange(range.first, range.last + 1)
) {

    override fun resolve(): PsiElement? {
        // Explicitly define the object as a PsiElement to satisfy the return type
        return object : FakePsiElement(), Navigatable {
            override fun getParent(): PsiElement = element

            override fun navigate(requestFocus: Boolean) {
                val project = element.project
                val state = ReqSettingsState.getInstance()
                val url = state.urlTemplate.replace("%s", value)

                thisLogger().warn("Loading URL: $url")

                MyProjectService.getInstance(project).load(url)

                // Ensure the tool window is visible so the user sees the result
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("ID Browser")
                toolWindow?.show()
            }

            override fun canNavigate(): Boolean = true
            override fun canNavigateToSource(): Boolean = false
            override fun getName(): String = value
        }
    }

    override fun isSoft(): Boolean = true
    override fun getVariants(): Array<Any> = emptyArray()
}
