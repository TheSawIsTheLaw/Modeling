import kotlin.math.round

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