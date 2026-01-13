package com.github.rob82926.idbrowserintellijplugin

import com.github.rob82926.idbrowserintellijplugin.settings.ReqSettingsState
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class ReqReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {

                    val state = ReqSettingsState.getInstance()
                    val regex = Regex(state.regex)

                    val matches = regex.findAll(element.text)
                    return matches.map {
                        ReqPsiReference(element, it.range, it.value)
                    }.toList().toTypedArray()
                }
            }
        )
    }
}
