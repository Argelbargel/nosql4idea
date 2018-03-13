package org.codinjutsu.tools.nosql.commons.view.scripting

internal class ConsoleLog(private val textArea: ScriptExecutor.MyTextPanel) {

    @Suppress("unused")
    fun log(message: String) {
        textArea.append("$message\n")
    }
}