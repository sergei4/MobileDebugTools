package me.sergei4.mobile.tools.mdbgui.os

import io.reactivex.Observable
import java.io.BufferedReader
import java.io.InputStreamReader

object ProcessHelper {

    fun execute(command: String, debug: Boolean = false): String {
        //Logger.d("Run: " + command);
        val output = StringBuilder()
        val p: Process
        try {
            p = Runtime.getRuntime().exec(command, emptyArray())

            val reader = BufferedReader(InputStreamReader(p.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.appendLine(line)
                //System.out.println(line);
            }
//            reader = BufferedReader(InputStreamReader(p.errorStream))
//            while (reader.readLine().also { line = it } != null) {
//                if (!firstLine) {
//                    output.append("\n")
//                }
//                firstLine = false
//                output.append(line).append("\n")
//                //System.out.println(line);
//            }
            p.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
            output.append("Execution error: ${e.message}")
        }
        return output.toString()
    }

    fun observeProcess(cmd: String) =
        Observable.create<String> { emitter ->
            try {
                val process = Runtime.getRuntime().exec(cmd, emptyArray())
                val reader = InputStreamReader(process.inputStream).buffered()

                var line: String? = null
                while (!emitter.isDisposed && reader.readLine().also { line = it } != null) {
                    line?.let { emitter.onNext(it) }
                }
                process.destroy()
                emitter.onComplete()
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
}