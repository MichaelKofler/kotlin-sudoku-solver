# Sudoku solver in Kotlin

This 

Usage:

```
val test = """
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
val sudoku = Sudoku(test)
sudoku.plot()  // plot unsolved Sudoku
println(sudoku.solveRecursive())
sudoku.plot()  // plot solution
```

The project is also documented in my (German) blog at <https://kofler.info/sudokus-mit-kotlin-loesen>.
