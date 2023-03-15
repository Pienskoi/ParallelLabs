import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.system.measureNanoTime

class Lab2(private val dimension: Int = 100) {
    private val B = generateRandomVector(dimension)
    private val D = generateRandomVector(dimension)
    private val MD = generateRandomMatrix(dimension, dimension)
    private val ME = generateRandomMatrix(dimension, dimension)
    private val MM = generateRandomMatrix(dimension, dimension)

    var MF: Matrix = Matrix(Array(MD.columnSize) { DoubleArray(MD.rowSize) })
    var E: Matrix = Matrix(Array(B.columnSize) { DoubleArray(B.rowSize) })

    private val dispatcher: ExecutorCoroutineDispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    fun main() {
        log("Writing inputs to files")
        writeMatrixToFile(B, "input", "B")
        writeMatrixToFile(D, "input", "D")
        writeMatrixToFile(MD, "input", "MD")
        writeMatrixToFile(ME, "input", "ME")
        writeMatrixToFile(MM, "input", "MM")

        log("Starting execution")
        val duration = measureNanoTime {
            val threads = mutableListOf<Thread>()
            val lockMF = ReentrantLock()
            val lockE = ReentrantLock()
            threads.add(thread { calculateMF(lockMF) })
            threads.add(thread { calculateE(lockE) })
            threads.forEach { it.join() }
        }
        dispatcher.close()
        val durationInMillis = duration / 1000000
        println("Execution took $durationInMillis milliseconds")

        log("Writing result to files")
        writeMatrixToFile(MF, "output/2", "MF")
        writeMatrixToFile(E, "output/2", "E")
    }

    // MF=MD*(ME+MM)-ME*MM
    private fun calculateMF(lockMF: Lock) = runBlocking {
        val jobs = mutableListOf<Job>()
        jobs.add(
            launch(dispatcher) {
                log("Calculating MD*(ME+MM)")
                val productMDsumMEMM = MD * (ME + MM)
                log("Calculation of MD*(ME+MM) is done")
                lockMF.lock()
                MF += productMDsumMEMM
                lockMF.unlock()
            }
        )
        jobs.add(
            launch(dispatcher) {
                log("Calculating ME*MM ...")
                val productMEMM = ME * MM
                log("Calculation of ME*MM is done")
                lockMF.lock()
                MF -= productMEMM
                lockMF.unlock()
            }
        )
        jobs.joinAll()
    }

    // E=В*МE+D*max(MM)
    private fun calculateE(lockE: Lock) = runBlocking {
        val jobs = mutableListOf<Job>()
        jobs.add(
            launch(dispatcher) {
                log("Calculating B*ME")
                val productBME = B * ME
                log("Calculation of B*ME is done")
                lockE.lock()
                E += productBME
                lockE.unlock()
            }
        )
        jobs.add(
            launch(dispatcher) {
                log("Calculating D*max(MM) ...")
                val productDmaxMM = D * MM.max()
                log("Calculation of D*max(MM) is done")
                lockE.lock()
                E += productDmaxMM
                lockE.unlock()
            }
        )
        jobs.joinAll()
    }
}
