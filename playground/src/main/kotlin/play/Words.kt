package play

import java.io.File


fun main(args: Array<String>) {
    val root = File("/Users/yarik/IdeaProjects/IDEA_COMMUNITY")

    val files: FileTreeWalk = root.walkTopDown().onEnter {
        val fileName = it.name
        val isTestDir = fileName == "testSrc" || fileName == "testData" || fileName == "test"
        !isTestDir
    }

    val set = mutableSetOf<String>()



    for (file in files) {
        println(file)
        if (file.name.endsWith(".kt") || file.name.endsWith(".java")) {
            file.readText()
                    .split(' ', '\n', '\t', '.')
                    .asSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach {
                        set.add(it)
                    }
        }
    }

    val resultFile = File("user_words")
    if (resultFile.exists()) {
        resultFile.delete()
    }
    resultFile.createNewFile()

    set.forEach {
        resultFile.appendText("$it\n")
    }


}