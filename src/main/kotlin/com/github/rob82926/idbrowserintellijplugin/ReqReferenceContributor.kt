package com.github.rob82926.idbrowserintellijplugin

import com.github.rob82926.idbrowserintellijplugin.settings.ReqSettingsState
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class ReqReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        thisLogger().warn("Registering ReqReferenceContributor")

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val text = element.text ?: return PsiReference.EMPTY_ARRAY
                    if (text.isEmpty()) return PsiReference.EMPTY_ARRAY

                    val state = ReqSettingsState.getInstance()
                    val regex = try {
                        Regex(state.regex)
                    } catch (e: Exception) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val matches = regex.findAll(text)
                    val result = matches.map { match ->
                        val start = match.range.first
                        val end = match.range.last + 1

                        thisLogger().info("Possible match: '${match.value}' at [$start, $end] in ${element.javaClass.simpleName}")

                        // Safety: ensure range is within element bounds
                        if (start >= 0 && end <= text.length && start < end) {
                            ReqPsiReference(element, IntRange(start, end - 1), match.value)
                        } else {
                            thisLogger().error("Invalid range for match '${match.value}': [$start, $end] text length: ${text.length}")
                            null
                        }
                    }.filterNotNull().toList()

                    if (result.isEmpty()) return PsiReference.EMPTY_ARRAY
                    return result.toTypedArray()
                }
            }
        )
    }
}
