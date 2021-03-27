import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.round

// Linear interpolation. Works only for (sort #'> table)
// Returns null/not null values so check it out properly
fun linearInterpolation(table: List<Pair<Double, Double>>, findX: Double): Double
{
    if (findX > table.last().first) return table.last().first
    if (findX <= table[0].first) return table[0].second

    var firstDot: Pair<Double, Double>? = null
    var secondDot: Pair<Double, Double>? = null

    var curPairInd: Int = 0
    while ((curPairInd < table.size) && (firstDot == null))
    {
        if (table[curPairInd].first >= findX)
        {
            firstDot = table[curPairInd - 1]
            secondDot = table[curPairInd]
        }
        curPairInd++
    }

    return firstDot!!.second +
            (secondDot!!.second - firstDot.second) / (secondDot.first - firstDot.first) * (findX - firstDot.first)
}

// trapezodial integration. Be careful with arguments: leftLimit > rightLimit and fragNum > 0
fun trapezodialIntegration(
    leftLimit: Double,
    rightLimit: Double,
    fragNum: Int,
    table: List<Pair<Double, Double>>) : Double
{
    val step: Double = (rightLimit - leftLimit) / fragNum
    var curX: Double = leftLimit
    var outSum: Double = 0.0
    var fInter: Double?
    var sInter: Double?
    for (ind in 0 until fragNum)
    {
        fInter = linearInterpolation(table, curX)
        sInter = linearInterpolation(table, curX + step)
        outSum += (fInter + sInter) / 2.0

        curX += step
    }

    return round((outSum * step) * 1e5) / 1e5
}

fun prepareSigmaT_Table(
    IT0_Table: List<Pair<Double, Double>>,
    Im_Table: List<Pair<Double, Double>>,
    Tsigma_Table: List<Pair<Double, Double>>,
    parameters: Map<String, Double>) : MutableList<Pair<Double, Double>>
{
    val outTable: MutableList<Pair<Double, Double>> = mutableListOf()

    var amperage: Double
    var currentT0: Double
    var currentM: Double
    var sigma: Double
    for (i in IT0_Table)
    {
        amperage = i.first
        currentT0 = linearInterpolation(IT0_Table, amperage)
        currentM = linearInterpolation(Im_Table, amperage)

        sigma = linearInterpolation(Tsigma_Table, currentT0 + (parameters["Tw"]!! - currentT0) * amperage) * amperage.pow(currentM)
        outTable.add(Pair(sigma, currentT0))
    }

    return outTable
}
fun findNonLinearResistance(Le: Double, Res: Double, TzTable: List<Pair<Double, Double>>): Double
{
    return Le / (PI * Res * Res * trapezodialIntegration(0.0, 1.0, 100, TzTable))
}

fun fFunction(curA: Double, curU: Double, parameters: Map<String, Double>, TzTable: List<Pair<Double, Double>>): Double
{
    return (curU - (parameters["Rk"]!! + findNonLinearResistance(parameters["Ie"]!!, parameters["R"]!!, TzTable)) * curA) / parameters["Lk"]!!
}

fun phiFunction(curA: Double, Ck: Double): Double
{
    return - curA / Ck
}

fun getNextAmperageVoltage(curA: Double,
                           curU: Double,
                           parameters: Map<String, Double>,
                           TzTable: List<Pair<Double, Double>>,
                           step: Double): Pair<Double, Double>
{
    val Ck = parameters["Ck"]!!

    val f1 = fFunction(curA, curU, parameters, TzTable)
    val phi1 = phiFunction(curA, Ck)
    val f2 = fFunction(curA + step * f1 / 2, curU + step * phi1 / 2, parameters, TzTable)
    val phi2 = phiFunction(curA + step * f1 / 2, Ck)
    val f3 = fFunction(curA + step * f2 / 2, curU + step * phi2 / 2, parameters, TzTable)
    val phi3 = phiFunction(curA + step * f2 / 2, Ck)
    val f4 = fFunction(curA + step * f3 / 2, curU + step * phi3 / 2, parameters, TzTable)
    val phi4 = phiFunction(curA + step * f3 / 2, Ck)

    return Pair(curA + step * (f1 + 2 * f2 + 2 * f3 + f4) / 6, curA + step * (phi1 + 2 * phi2 + 2 * phi3 + phi4) / 6)
}

fun main()
{
    val IT0_Table: List<Pair<Double, Double>> =
        listOf(
            Pair(0.5, 6730.0),
            Pair(1.0, 6790.0),
            Pair(5.0, 7150.0),
            Pair(10.0, 7270.0),
            Pair(50.0, 8010.0),
            Pair(200.0, 9185.0),
            Pair(400.0, 10010.0),
            Pair(800.0, 11140.0),
            Pair(1200.0, 12010.0)
        )

    val Im_Table: List<Pair<Double, Double>> =
        listOf(
            Pair(0.5, 0.5),
            Pair(1.0, 0.55),
            Pair(5.0, 1.7),
            Pair(10.0, 3.0),
            Pair(50.0, 11.0),
            Pair(200.0, 32.0),
            Pair(400.0, 40.0),
            Pair(800.0, 41.0),
            Pair(1200.0, 39.0))

    val Tsigma_Table: List<Pair<Double, Double>> =
        listOf(
            Pair(4000.0, 0.031),
            Pair(5000.0, 0.27),
            Pair(6000.0, 2.05),
            Pair(7000.0, 6.06),
            Pair(8000.0, 12.0),
            Pair(9000.0, 19.9),
            Pair(10000.0, 29.6),
            Pair(11000.0, 41.1),
            Pair(12000.0, 54.1),
            Pair(13000.0, 67.7),
            Pair(14000.0, 81.5)
        )

    val parameters: Map<String, Double> =
        mapOf("R" to 0.35, "Ie" to 12.0, "Lk" to 187 * 1e-6, "Ck" to 268 * 1e-6, "Rk" to 0.25,
            "Uco" to 1400.0, "Tw" to 2000.0)

    val TzTable = prepareSigmaT_Table(IT0_Table, Im_Table, Tsigma_Table, parameters)

    var curT: Double = 0.0
    val step: Double = 1e-5
    var currentAmperage: Double = 0.0
    var currentVoltage: Double = 1400.0

    val outTableIT: MutableList<Pair<Double, Double>> = mutableListOf()
    val outTableUT: MutableList<Pair<Double, Double>> = mutableListOf()

    for (i in 0 until 1200)
    {
        val curPair = getNextAmperageVoltage(currentAmperage, currentVoltage, parameters, TzTable, step)
        currentAmperage = curPair.first
        currentVoltage = curPair.second

        outTableIT.add(Pair(currentAmperage, curT))
        outTableUT.add(Pair(currentVoltage, curT))

        curT += step
    }

}