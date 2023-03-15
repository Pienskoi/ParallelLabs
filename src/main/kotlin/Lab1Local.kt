import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.system.measureNanoTime

class Lab1Local {
    lateinit var MF: Matrix
    lateinit var E: Matrix

    private val dispatcher: ExecutorCoroutineDispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    fun main(dimension: Int = 100) {
        log("Generating inputs")
        val B = generateRandomVector(dimension)
        val D = generateRandomVector(dimension)
        val MD = generateRandomMatrix(dimension, dimension)
        val ME = generateRandomMatrix(dimension, dimension)
        val MM = generateRandomMatrix(dimension, dimension)

        log("Writing inputs to files")
        writeMatrixToFile(B, "input", "B")
        writeMatrixToFile(D, "input", "D")
        writeMatrixToFile(MD, "input", "MD")
        writeMatrixToFile(ME, "input", "ME")
        writeMatrixToFile(MM, "input", "MM")

        log("Starting execution")
        val duration = measureNanoTime {
            val threads = mutableListOf<Thread>()
            threads.add(thread { calculateMF(MD, ME, MM) })
            threads.add(thread { calculateE(B, D, ME, MM) })
            threads.forEach { it.join() }
        }
        dispatcher.close()
        val durationInMillis = duration / 1000000
        println("Execution took $durationInMillis milliseconds")

        log("Writing result to files")
        writeMatrixToFile(MF, "output/1", "MF")
        writeMatrixToFile(E, "output/1", "E")
    }

    // MF=MD*(ME+MM)-ME*MM
    private fun calculateMF(md: Matrix, me: Matrix, mm: Matrix) = runBlocking {
        val productMDsumMEMM = async(dispatcher) {
            log("Calculating MD*(ME+MM)")
            val result = md * (me + mm)
            log("Calculation of MD*(ME+MM) is done")
            result
        }
        val productMEMM = async(dispatcher) {
            log("Calculating ME*MM ...")
            val result = me * mm
            log("Calculation of ME*MM is done")
            result
        }
        log("Calculating MF ...")
        MF = productMDsumMEMM.await() - productMEMM.await()
        log("Calculation of MF is done")
    }

    // E=В*МE+D*max(MM)
    private fun calculateE(b: Matrix, d: Matrix, me: Matrix, mm: Matrix) = runBlocking {
        val productBME = async(dispatcher) {
            log("Calculating B*ME ...")
            val result = b * me
            log("Calculation of B*ME is done")
            result
        }
        val productDmaxMM = async(dispatcher) {
            log("Calculating D*max(MM) ...")
            val result = d * mm.max()
            log("Calculation of D*max(MM) is done")
            result
        }
        log("Calculating E ...")
        E = productBME.await() + productDmaxMM.await()
        log("Calculation of E is done")
    }
}
