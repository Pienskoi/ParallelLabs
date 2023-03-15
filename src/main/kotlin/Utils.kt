import java.io.File
import kotlin.math.pow
import kotlin.random.Random

fun writeMatrixToFile(matrix: Matrix, directoryPath: String, filename: String) {
    val directory = File(directoryPath)
    if (!directory.exists()) {
        directory.mkdirs()
    }
    val file = File(directory, filename)
    file.writeText(matrix.toString())
}

fun generateRandomMatrix(n: Int, m: Int): Matrix {
    val array = Array(n) { DoubleArray(m) { Random.nextDouble() * 10.0.pow(Random.nextInt(100)) } }
    return Matrix(array)
}

fun generateRandomVector(n: Int): Matrix = generateRandomMatrix(1, n)

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")
