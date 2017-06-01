package play

import java.io.File


fun main(args: Array<String>) {
    val root = File("/Users/yarik/IdeaProjects/ScreenShooter/dictionary")

    val files: FileTreeWalk = root.walkTopDown()

    val set = mutableSetOf<String>()
    for (file in files) {
        set.addAll(file.readLines().map { it.trim() }.filter { it.isNotEmpty() })
    }

    val resultFile = File("dict")
    if (resultFile.exists()) {
        resultFile.delete()
    }
    resultFile.createNewFile()

    set.forEach {
        resultFile.appendText("$it\n")
    }


}