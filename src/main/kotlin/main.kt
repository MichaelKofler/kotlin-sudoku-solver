fun main(args: Array<String>) {

    // wrong sudoku, can't be solved
    val wrong = """
        |34......5
        |.867.....
        |...8..19.
        |.32.1....
        |.5.....7.
        |....8.34.
        |.61..9...
        |.....852.
        |5.......3
    """.trimMargin()

    // totally empty sudoku
    val empty = """
        |.........
        |.........
        |.........
        |.........
        |.........
        |.........
        |.........
        |.........
        |.........
    """.trimMargin()

    // https://www.derstandard.at/story/2000123424134/sudoku-mittel-4821a
    val medium = """
        ..27.46..
        ......2.7
        8......9.
        7..386.4.
        .3..9..8.
        .8.517..3
        .6......5
        2.8......
        ..18.94..
    """.trimIndent()

    // https://www.derstandard.at/story/2000123423977/sudoku-schwierig-4820a
    val difficult1 = """
        3192.....
        ......7.1
        ...63....
        67.8.35..
        9.......3
        ..21.5.47
        ....62...
        2.3......
        .....7259
    """.trimIndent()

    // https://www.derstandard.at/story/2000123424463/sudoku-extrem-schwierig-4824a
    val difficult2 = """
        3.......5
        .867.....
        ...8..19.
        .32.1....
        .5.....7.
        ....8.34.
        .61..9...
        .....852.
        5.......3
    """.trimIndent()

    // https://www.derstandard.at/story/2000123387616/sudoku-extrem-schwierig-4818a
    val difficult3 = """
        ...41...3
        7....2..5
        .58.7....
        5....72..
        ..2...5..
        ..72....1
        ....9.62.
        3..5....4
        6...84...
    """.trimIndent()

    // https://sudoku9x9.com/expert.php  (L6: #812518581)
    val expert = """
        8......3.
        ........5
        9..7..1.4
        ..186..2.
        .7.1...5.
        3....4...
        62....9..
        .....1...
        .59..32..
    """.trimIndent()

    // test code
    val sudoku = Sudoku(difficult2)
    sudoku.plot()
    println(sudoku.solveRecursive())
    sudoku.plot()
}
