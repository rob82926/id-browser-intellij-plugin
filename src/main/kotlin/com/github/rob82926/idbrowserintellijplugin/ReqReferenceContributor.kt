package com.github.rob82926.idbrowserintellijplugin

import com.github.rob82926.idbrowserintellijplugin.settings.ReqSettingsState
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileTypes.FileType
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class ReqReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        thisLogger().warn("Registering ReqReferenceContributor")

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiComment::class.java),
            object : PsiReferenceProvider() {

                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {

                    val file = element.containingFile ?: return PsiReference.EMPTY_ARRAY
                    val virtualFile = file.virtualFile ?: return PsiReference.EMPTY_ARRAY

                    // Skip binary files explicitly
                    val fileType: FileType = virtualFile.fileType
                    if (fileType.isBinary) return PsiReference.EMPTY_ARRAY

                    // Skip injected / synthetic PSI
                    if (!element.isValid || element.textRange == null) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val text = element.text
                    if (text.isNullOrEmpty()) return PsiReference.EMPTY_ARRAY

                    val state = ReqSettingsState.getInstance()
                    val regex = try {
                        Regex(state.regex)
                    } catch (e: Exception) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val references = mutableListOf<PsiReference>()

                    for (match in regex.findAll(text)) {
                        val start = match.range.first
                        val endExclusive = match.range.last + 1

                        if (start >= 0 && endExclusive <= text.length && start < endExclusive) {
                            thisLogger().info(
                                "Possible match: '${match.value}' at [$start, $endExclusive] " +
                                        "in ${file.name} (${element.javaClass.simpleName})"
                            )

                            references += ReqPsiReference(
                                element,
                                IntRange(start, endExclusive - 1),
                                match.value
                            )
                        } else {
                            thisLogger().error(
                                "Invalid range for match '${match.value}': " +
                                        "[$start, $endExclusive], text length=${text.length}"
                            )
                        }
                    }

                    return if (references.isEmpty()) {
                        PsiReference.EMPTY_ARRAY
                    } else {
                        references.toTypedArray()
                    }
                }
            }
        )
    }
}
