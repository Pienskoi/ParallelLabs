class Matrix(val matrix: Array<DoubleArray>) {
    val columnSize
        get() = matrix.size
    val rowSize
        get() = matrix[0].size

    private fun kahanSum(numbers: DoubleArray): Double {
        var sum = 0.0
        var c = 0.0
        for (x in numbers) {
            val y = x - c
            val t = sum + y
            c = (t - sum) - y
            sum = t
        }
        return sum
    }

    operator fun plus(other: Matrix): Matrix {
        require(columnSize == other.columnSize && rowSize == other.rowSize) {
            "Matrices must have an equal number of rows and columns to be added"
        }

        val result = Array(columnSize) { DoubleArray(rowSize) { 0.0 } }
        for (i in 0 until columnSize) {
            for (j in 0 until rowSize) {
                val a = matrix[i][j]
                val b = other.matrix[i][j]
                val sum = kahanSum(doubleArrayOf(a, b))
                result[i][j] = sum
            }
        }
        return Matrix(result)
    }

    operator fun minus(other: Matrix): Matrix {
        require(columnSize == other.columnSize && rowSize == other.rowSize) {
            "Matrices must have an equal number of rows and columns to be subtracted"
        }

        val result: Array<DoubleArray> = Array(columnSize) { DoubleArray(rowSize) { 0.0 } }
        for (i in 0 until columnSize) {
            for (j in 0 until rowSize) {
                val a = matrix[i][j]
                val b = other.matrix[i][j]
                val sum = kahanSum(doubleArrayOf(a, -b))
                result[i][j] = sum
            }
        }
        return Matrix(result)
    }

    operator fun times(other: Matrix): Matrix {
        require(rowSize == other.columnSize) {
            "The number of columns in the first matrix must be equal to the number of rows in the second matrix"
        }

        val result = Array(columnSize) { DoubleArray(rowSize) { 0.0 } }
        for (i in 0 until columnSize) {
            for (j in 0 until rowSize) {
                var products = DoubleArray(rowSize)
                for (k in 0 until other.columnSize) {
                    val a = matrix[i][k]
                    val b = other.matrix[k][j]
                    products += a * b
                }
                val sum = kahanSum(products)
                result[i][j] = sum
            }
        }
        return Matrix(result)
    }

    operator fun times(other: Double): Matrix {
        val result = Array(columnSize) { DoubleArray(rowSize) { 0.0 } }
        for (i in 0 until columnSize) {
            for (j in 0 until rowSize) {
                val a = matrix[i][j]
                val product = a * other
                result[i][j] = product
            }
        }
        return Matrix(result)
    }

    fun max(): Double {
        var max = Double.NEGATIVE_INFINITY
        for (row in matrix) {
            for (value in row) {
                if (value > max) {
                    max = value
                }
            }
        }
        return max
    }

    fun copy(): Matrix {
        val copy = Array(columnSize) { i -> matrix[i].copyOf() }
        return Matrix(copy)
    }

    override fun toString(): String {
        return matrix.joinToString(separator = "\n") { row -> row.joinToString(separator = " ") }
    }
}
