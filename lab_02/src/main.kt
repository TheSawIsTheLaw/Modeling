import kotlin.math.round

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
    mapOf(Pair("R", 0.35), Pair("Ie", 12.0), Pair("Lk", 187 * 1e-6), Pair("Ck", 268 * 1e-6), Pair("Rk", 0.25),
        Pair("Uco", 1400.0), Pair("Tw", 2000.0))
// Linear interpolation. Works only for (sort #'> table)
// Returns null/not null values so check it out properly
fun linearInterpolation(table: List<Pair<Double, Double>>, findX: Double): Double
{
    if (findX > table.last().first) return table.last().first
    if (findX == table[0].first) return table[0].second

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

    println(outSum)

    return round((outSum * step) * 1e5) / 1e5
}

fun main()
{
    val table: List<Pair<Double, Double>> = listOf(Pair(.0, .0), Pair(1.0, 1.0), Pair(2.0, 2.0))

    println(linearInterpolation(table, 1.95000000000001))
    val out = trapezodialIntegration(0.0, 2.0, 40, table)
    println("Outer result is: $out")
}