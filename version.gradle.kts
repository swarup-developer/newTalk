import java.util.Properties

val versionProps = Properties().apply {
    val propFile = rootProject.file("version.properties")
    if (propFile.exists()) {
        propFile.inputStream().use { load(it) }
    }
}

val talkbackVersionName = versionProps.getProperty("versionName", "TfPu_release_16_2")
val buildNumberOffset = versionProps.getProperty("buildNumberOffset", "20").toInt()
val backtalkVersionCodeBase = versionProps.getProperty("versionCodeBase", "70000000").toInt()

fun gitOutput(args: List<String>): String = try {
    providers.exec {
        commandLine(listOf("git") + args)
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()
} catch (e: Exception) {
    ""
}

val commitCountStr = gitOutput(listOf("rev-list", "--count", "HEAD"))
val commitHashStr = gitOutput(listOf("rev-parse", "HEAD"))
val gitCommitCount = (commitCountStr.toIntOrNull() ?: 1) + buildNumberOffset
val gitCommitHash = if (commitHashStr.matches(Regex("[0-9a-f]{40}"))) commitHashStr else ""

extra["talkbackVersionName"] = talkbackVersionName
extra["buildNumberOffset"] = buildNumberOffset
extra["backtalkVersionCodeBase"] = backtalkVersionCodeBase
extra["gitCommitCount"] = gitCommitCount
extra["gitCommitHash"] = gitCommitHash
