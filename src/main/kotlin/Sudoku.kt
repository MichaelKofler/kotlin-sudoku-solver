// see https://github.com/marcelmatula/colored-console
import com.github.mm.coloredconsole.colored

// possible results of solve()
enum class Result { Finished, Failure, Open; }

// possible next move
data class Move(val row: Int, val col: Int, val nmb: Int)

// data: nine line string containing numbers of sudoku
//       '0' or '.' or ' ': field is empty
@Suppress("ControlFlowWithEmptyBody")
class Sudoku(data: String) {
    // sudoku: initally empty 9x9 list, to be initalized in constructor
    private val sudoku = List(9) { MutableList(9) { 0 } }

    // set of candidates for each cell of sudoku
    private var candidates =
        List(9) { MutableList(9) { mutableSetOf(1, 2, 3, 4, 5, 6, 7, 8, 9) } }

    // computed properties for sudoku status
    private val statusCellsDone get() = sudoku.flatten().count { it != 0 }
    private val statusCellsOpen get() = 81 - statusCellsDone
    private val statusCandidatesCount get() = candidates.flatten().sumBy { it.size }
    private val statusFailedCells get() = countFailedCells()

    // debugging
    private var debugNoOfTries = 0
    private var debugRecLevel = 0
    private val debugShowText = true
    private val debugShowPlot = false

    // constructor, initializes sudoku list from data;
    // causes exception if data has more or less than 9 lines
    // or any line has less than 9 characters
    init {
        for ((row, line) in data.lines().withIndex()) {
            for (col in 0..8) {
                val nmb = line[col].toString().toIntOrNull() ?: 0
                sudoku[row][col] = nmb
                if (nmb != 0)
                // empty set of candidates
                    candidates[row][col] = mutableSetOf()
            }
        }
        eliminateCandidates()
    }

    // count empty cells with no candidates left
    private fun countFailedCells() : Int {
        var failedCells = 0
        for (row in 0..8)
            for (col in 0..8)
                if (sudoku[row][col] == 0 && candidates[row][col].size == 0)
                    failedCells++
        return failedCells
    }

    // indent line according to recursion level
    private fun indent() {
        print("  ".repeat(debugRecLevel))
    }

    // output current state of Sudoko including solved cells (bold, black)
    // and possible candidates (grey)
    fun plot(markrow: Int = -1, markcol: Int = -1) {
        colored {
            for (row in 0..8) {
                println()

                // Line 1: candidates 1 2 3
                indent()
                for (col in 0..8) {
                    if (col % 3 == 0)
                        print("   ")
                    else
                        print(" ")
                    val nmb = sudoku[row][col]
                    val i = if (1 in candidates[row][col]) "1" else " "
                    val j = if (2 in candidates[row][col]) "2" else " "
                    val k = if (3 in candidates[row][col]) "3" else " "
                    if (nmb == 0)
                        print(" $i $j $k ".black.bright.white.bg)
                    else
                        print("       ".white.bg)
                }
                println()

                // Line 2: candidates 4 5 6
                indent()
                for (col in 0..8) {
                    if (col % 3 == 0)
                        print("   ")
                    else
                        print(" ")
                    val nmb = sudoku[row][col]
                    val i = if (4 in candidates[row][col]) "4" else " "
                    val j = if (5 in candidates[row][col]) "5" else " "
                    val k = if (6 in candidates[row][col]) "6" else " "
                    if (nmb == 0)
                        print(" $i $j $k ".black.bright.white.bg)
                    else
                        if (row == markrow && col == markcol)
                            print("   $nmb   ".red.bold.white.bg)
                        else
                            print("   $nmb   ".bold.white.bg)
                }
                println()

                // Line 3: candidates 7 8 9
                indent()
                for (col in 0..8) {
                    if (col % 3 == 0)
                        print("   ")
                    else
                        print(" ")
                    val nmb = sudoku[row][col]
                    val i = if (7 in candidates[row][col]) "7" else " "
                    val j = if (8 in candidates[row][col]) "8" else " "
                    val k = if (9 in candidates[row][col]) "9" else " "
                    if (nmb == 0)
                        print(" $i $j $k ".black.bright.white.bg)
                    else
                        print("       ".white.bg)
                }
                println()
                if ((row - 2) % 3 == 0)
                    println()
            }
        }
    }

    // try to solve Sudoku (non-recursive)
    private fun solve(): Result {
        do {
            val todo = statusCellsOpen
            val options = statusCandidatesCount
            // call methods repeatedly, until they make no longer progress
            do while (findNakedSingle())
            do while (findHiddenSingleInRow())
            do while (findHiddenSingleCol())
            do while (findHiddenSingleBox())
            // optimize candidate sets
            eliminatePairNumbers()
            eliminateBoxBlockingNumbers()
            // try again, until neither number unsolved sells
            // nor number of open candidates drop
        } while (statusCellsOpen < todo || statusCandidatesCount < options)

        return when {
            statusCellsOpen == 0   -> Result.Finished  // all done
            statusFailedCells > 0  -> Result.Failure   // empty cells without candidates
            else                   -> Result.Open      // uncertain
        }
    }

    // try to solve Sudokue recursively
    fun solveRecursive(): Result {
        val result = solve()

        // only continue if unfinished
        if (result == Result.Open) {
            // collect possibilities;
            var choices = listOf<Move>()
            // get first cell with as little candidates as possible
            findcell@
            for (nmb in 2..9)
                for (row in 0..8)
                    for (col in 0..8)
                        if (candidates[row][col].size == nmb) {
                            choices = candidates[row][col].map { Move(row, col, it) }
                            break@findcell
                        }
            choices = choices.reversed()
            debugPrintln("Possible choices to continue: ${choices.size} $choices")

            // try them out
            for ((moveNo, move) in choices.withIndex()) {
                debugPrintln("Try recursively ${moveNo + 1} / ${choices.size}")
                debugRecLevel++
                // make backup of current state of sudoku
                // (flatten gives a sufficiently 'deep copy' here)
                val sudokuBackup = sudoku.flatten()
                setCell(move.row, move.col, move.nmb, "Guess")
                // recursive call; result can only be
                // .Failure or .Success, as .Open is
                // handled internally
                val recResult = solveRecursive()
                debugRecLevel--
                if (recResult == Result.Finished) {
                    // all done
                    return recResult
                } else {
                    debugPrintln("$recResult, try next.")
                    // restore Sudoku + candidates, try next
                    var cnt=0
                    for (row in 0..8)
                        for (col in 0..8) {
                            sudoku[row][col] = sudokuBackup[cnt]
                            cnt++
                            if (sudoku[row][col] == 0)
                                candidates[row][col] = mutableSetOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
                            else
                                candidates[row][col] = mutableSetOf()
                        }
                    eliminateCandidates()
                    eliminatePairNumbers()
                    eliminateBoxBlockingNumbers()
                }
            }
        }
        return result
    }

    // eliminate candidates according to filled cells in row/col/box
    private fun eliminateCandidates() {
        for (row in 0..8)
            for (col in 0..8) {
                var candSet = candidates[row][col]
                candSet = eliminateCandidatesRow(candSet, row)
                candSet = eliminateCandidatesCol(candSet, col)
                candSet = eliminateCandidatesBox(candSet, row, col)
                candidates[row][col] = candSet
            }
    }

    // eliminate candidates for filled cells in row
    private fun eliminateCandidatesRow(cand: MutableSet<Int>, row: Int)
            : MutableSet<Int> {
        for (col in 0..8)
            cand.remove(sudoku[row][col])
        return cand
    }

    // eliminate candidates for filled cells in col
    private fun eliminateCandidatesCol(cand: MutableSet<Int>, col: Int)
            : MutableSet<Int> {
        for (row in 0..8)
            cand.remove(sudoku[row][col])
        return cand
    }

    // eliminate candidates for filled cells in col
    private fun eliminateCandidatesBox(cand: MutableSet<Int>, row: Int, col: Int)
            : MutableSet<Int> {
        val rowstart = (row / 3) * 3  // start of box
        val colstart = (col / 3) * 3

        for (r in rowstart until rowstart + 3)
            for (c in colstart until colstart + 3)
                cand.remove(sudoku[r][c])
        return cand
    }

    // update candidate sets after cell @ row0/col0 has changed
    private fun updateCandidates(row0: Int, col0: Int) {
        // it would be simpler to call eliminateCandidates();
        // this is a little bit more efficient
        for (row in 0..8) {
            var tmp = candidates[row][col0]
            tmp = eliminateCandidatesRow(tmp, row)
            tmp = eliminateCandidatesCol(tmp, col0)
            tmp = eliminateCandidatesBox(tmp, row, col0)
            candidates[row][col0] = tmp
        }
        for (col in 0..8) {
            var tmp = candidates[row0][col]
            tmp = eliminateCandidatesRow(tmp, row0)
            tmp = eliminateCandidatesCol(tmp, col)
            tmp = eliminateCandidatesBox(tmp, row0, col)
            candidates[row0][col] = tmp
        }

        val rowstart = (row0 / 3) * 3  // start of box
        val colstart = (col0 / 3) * 3
        for (row in rowstart until rowstart + 3)
            for (col in colstart until colstart + 3) {
                var tmp = candidates[row][col]
                tmp = eliminateCandidatesRow(tmp, row)
                tmp = eliminateCandidatesCol(tmp, col)
                tmp = eliminateCandidatesBox(tmp, row, col)
                candidates[row][col] = tmp
            }
    }

    // loop over all candidate sets; if there is a set with exact one item
    // set cell accordingly, recalculate candidates and return true;
    // else: return false
    private fun findNakedSingle(): Boolean {
        for (row in 0..8)
            for (col in 0..8)
                if (candidates[row][col].size == 1) {
                    val nmb = candidates[row][col].toList()[0]
                    setCell(row, col, nmb, "Naked Single")
                    return true
                }

        return false
    }

    // found solution for one cell:
    // update sudoku + candidates lists, optionally print sudoku
    private fun setCell(row: Int, col: Int, nmb: Int, why: String = "unknown") {
        debugNoOfTries++
        sudoku[row][col] = nmb
        candidates[row][col] = mutableSetOf()
        updateCandidates(row, col)
        // debugging output
        debugPrintln("%-20s %d @ row %d | col %d".format(why, nmb, row + 1, col + 1))
        if (debugShowPlot)
            plot(row, col)
    }

    // within a row, is there a number which only appears once?
    private fun findHiddenSingleInRow(): Boolean {
        for (row in 0..8) {
            for (nmb in 1..9) {
                // is there a number, which is only in ONE candidate set?
                if (candidates[row].count { it.contains(nmb) } == 1)
                // yes, find it
                    for (col in 0..8)
                        if (candidates[row][col].contains(nmb)) {
                            // got it
                            setCell(row, col, nmb, "Hidden Single Row")
                            return true
                        }

            }
        }
        return false
    }

    // within a column, is there a number which only appears once?
    private fun findHiddenSingleCol(): Boolean {
        for (col in 0..8) {
            // bundle cells of a row in a list
            val candlist = mutableListOf<MutableSet<Int>>()
            for (row in 0..8)
                candlist += candidates[row][col]

            // find unique number in list
            for (nmb in 1..9) {
                // is there a number, which is only in ONE candidate set?
                if (candlist.count { it.contains(nmb) } == 1)
                // yes, find it
                    for (row in 0..8)
                        if (candidates[row][col].contains(nmb)) {
                            // got it
                            setCell(row, col, nmb, "Hidden Single Row")
                            return true
                        }

            }
        }
        return false
    }

    // within a box, is there a number which only appears once?
    private fun findHiddenSingleBox(): Boolean {
        // loop over all boxes
        for (rowstart in 0..6 step 3)
            for (colstart in 0..6 step 3) {
                // within box: loop over all cells,
                // bundle these cells in a list
                val candlist = mutableListOf<MutableSet<Int>>()
                for (row in rowstart..rowstart + 2)
                    for (col in colstart..colstart + 2)
                        candlist += candidates[row][col]

                // find unique number in list
                for (nmb in 1..9) {
                    // is there a number, which is only in ONE candidate set?
                    if (candlist.count { it.contains(nmb) } == 1)
                    // yes, find it
                        for (row in rowstart..rowstart + 2)
                            for (col in colstart..colstart + 2)
                                if (candidates[row][col].contains(nmb)) {
                                    // got it
                                    setCell(row, col, nmb, "Hidden Single Box")
                                    return true
                                }
                }
            }
        return false
    }

    // are there pairs of identical 2-sets in rows/cols/boxes
    // if so, elimnate the numbers from other sets in rows/cols/boxes
    private fun eliminatePairNumbers() {
        // needs sometimes more runs to find all options
        do {
            val oldcnt = statusCandidatesCount
            eliminatePairRows()
            eliminatePairColumns()
            eliminatePairBoxes()
        } while (statusCandidatesCount < oldcnt)
    }

    // is there a pair of identical 2-sets in row?
    // if so, elimnate the numbers from other sets in row
    private fun eliminatePairRows() {
        // for all rows
        for (row in 0..8) {
            val pair = findSet2Duplicate(candidates[row])
            if (pair.size == 2) {
                // eliminate these numbers from other sets in row
                for (col in 0..8) {
                    if (candidates[row][col] != pair && candidates[row][col].isNotEmpty()) {
                        candidates[row][col] = (candidates[row][col] - pair) as MutableSet<Int>
                    }
                }
            }
        }
    }

    // same for columns
    private fun eliminatePairColumns() {
        // for all columns
        for (col in 0..8) {
            // collect all sets of column into new list
            val sets = mutableListOf<Set<Int>>()
            for (row in 0..8)
                sets += candidates[row][col]
            // find duplicates
            val pair = findSet2Duplicate(sets)
            if (pair.size == 2) {
                // eliminate these numbers from other sets in row
                for (row in 0..8) {
                    if (candidates[row][col] != pair) {
                        candidates[row][col] = (candidates[row][col] - pair) as MutableSet<Int>
                    }
                }
            }
        }
    }

    // same for boxes
    private fun eliminatePairBoxes() {
        // for all boxes
        for (rowstart in 0 until 6 step 3) {
            for (colstart in 0 until 6 step 3) {
                // collect all sets of column into new list
                val sets = mutableListOf<Set<Int>>()
                for (row in rowstart until rowstart + 3)
                    for (col in colstart until colstart + 3)
                        sets += candidates[row][col]

                // find duplicates
                val pair = findSet2Duplicate(sets)
                if (pair.size == 2) {
                    // eliminate these numbers from other sets in row
                    for (row in rowstart until rowstart + 3)
                        for (col in colstart until colstart + 3)
                            if (candidates[row][col] != pair) {
                                candidates[row][col] = (candidates[row][col] - pair) as MutableSet<Int>
                            }
                }
            }
        }
    }

    // find two identical sets with two items
    private fun findSet2Duplicate(sets: List<Set<Int>>): Set<Int> {
        // makes only sense if there are at least 3 sets
        if (sets.count { it.isNotEmpty() } > 2)
        // look for duplicates ...
            for (i in 0..7)
            // but only for sets of two
                if (sets[i].size == 2)
                    for (j in i + 1 until 9)
                        if (sets[i] == sets[j])
                            return sets[i]

        // nothing found: return empty set
        return setOf()
    }

    // if a number must be placed in one mini-row or one mini-column
    // within a box, eliminate this number from the candidate sets in
    // the rest of the row/column
    // see images/sudoku-boxblock.png
    private fun eliminateBoxBlockingNumbers() {
        // needs sometimes more runs to find all options
        do {
            val oldcnt = statusCandidatesCount
            // loop over all boxes
            for (rowoffset in 0 until 6 step 3)
                for (coloffset in 0 until 6 step 3)

                // within box: loop over all cells
                    for (row in rowoffset until rowoffset + 3)
                        for (col in coloffset until coloffset + 3) {

                            // within cell: loop over numbers in candidate set
                            for (nmb in candidates[row][col]) {
                                // is this number in no other row of box?
                                if (boxBlockAllInSameRow(rowoffset, coloffset, row, col, nmb))
                                // -> remove number from candidates in
                                // the same row @ other boxes
                                    boxBlockEliminateNumberInRow(coloffset, row, nmb)

                                // is this number in no other column of box?
                                if (boxBlockAllInSameCol(rowoffset, coloffset, row, col, nmb))
                                // -> remove number from candidates in
                                // the same column @ other boxes
                                    boxBlockEliminateNumberInCol(rowoffset, col, nmb)
                            }
                        }
        } while (statusCandidatesCount < oldcnt)
    }

    // is the same number in a different row (other than row0)
    // within the box?
    private fun boxBlockAllInSameRow(
        rowoffset: Int, coloffset: Int,   // box to test
        row0: Int, col0: Int,             // start position
        nmb: Int
    ): Boolean               // number to find
    {
        for (row in rowoffset until rowoffset + 3)
            for (col in coloffset until coloffset + 3) {
                if (row == row0 && col == col0) continue
                if (candidates[row][col].contains(nmb) && row != row0)
                    return false
            }

        return true
    }

    // is the same number in a different col (other than col0)
    // within the box?
    private fun boxBlockAllInSameCol(
        rowoffset: Int, coloffset: Int,   // box to test
        row0: Int, col0: Int,             // start position
        nmb: Int
    ): Boolean               // number to find
    {
        for (row in rowoffset until rowoffset + 3)
            for (col in coloffset until coloffset + 3) {
                if (row == row0 && col == col0) continue
                if (candidates[row][col].contains(nmb) && col != col0)
                    return false
            }

        return true
    }

    // eliminate number in row (but spare box @ coloffset)
    private fun boxBlockEliminateNumberInRow(
        coloffset: Int,                   // box nmb is within
        row: Int,                         // row to eliminate nmb
        nmb: Int
    ) {
        for (col in 0..8) {
            // don't change candidate sets within the box
            if (col >= coloffset && col < coloffset + 3) continue
            candidates[row][col].remove(nmb)
        }
    }

    // eliminate number in column (but spare box @ rowoffset)
    private fun boxBlockEliminateNumberInCol(
        rowoffset: Int,                   // box nmb is within
        col: Int,                         // row to eliminate nmb
        nmb: Int
    ) {
        for (row in 0..8) {
            // don't change candidate sets within the box
            if (row >= rowoffset && row < rowoffset + 3) continue
            candidates[row][col].remove(nmb)
        }
    }

    // output debug message indented according to recursion level
    private fun debugPrintln(msg: String) {
        if (debugShowText) {
            indent()
            println(msg)
        }
    }
}
