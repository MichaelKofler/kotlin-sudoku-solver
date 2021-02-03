# Sudoku solver in Kotlin

This small IntelliJ project contains Kotlin code to solve sudokus. The class `Sudoku` has two main methods: `solve` tries to non-recursively solve the sudoku. It fails for difficult sudokus. `solveRecursive` does better. It also calls `solve` and start's guessing, when `solve` can't find a way.

If your soduku has more than one solutions, `solve` or `solveRecursive` get you just *one* solution.

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
