package com.example.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStreamWriter

class AutoServiceProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val serviceImplementations = mutableMapOf<String, MutableSet<String>>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("com.example.annotations.AutoService")

        for (symbol in symbols) {
            if (symbol !is KSClassDeclaration || symbol.classKind != ClassKind.CLASS) continue

            val implClassName = symbol.qualifiedName?.asString() ?: continue

            val superTypes = symbol.superTypes
                .map { it.resolve() }
                .filter {
                    val decl = it.declaration as? KSClassDeclaration
                    decl != null && decl.classKind == ClassKind.INTERFACE
                }

            for (interfaceType in superTypes) {
                val interfaceFqName =
                    interfaceType.declaration.qualifiedName?.asString() ?: continue
                serviceImplementations.getOrPut(interfaceFqName) { mutableSetOf() } += implClassName
            }
        }

        return emptyList()
    }

    override fun finish() {
        // 所有符号处理完后统一生成文件
        for ((interfaceFqName, implClassNames) in serviceImplementations) {
            try {
                // aggregating = true 表示聚合所有输入
                val dependencies = Dependencies(aggregating = true)
                val file = codeGenerator.createNewFile(
                    dependencies = dependencies,
                    packageName = "META-INF.services",
                    fileName = interfaceFqName,
                    extensionName = ""
                )

                OutputStreamWriter(file).use { writer ->
                    for (implName in implClassNames) {
                        writer.write(implName)
                        writer.write("\n")
                    }
                }

                file.close()
                logger.warn("Generated service: $interfaceFqName with ${implClassNames.size} implementations")

            } catch (e: Exception) {
                logger.error("Failed to generate service file for $interfaceFqName: ${e.message}")
            }
        }
    }
}