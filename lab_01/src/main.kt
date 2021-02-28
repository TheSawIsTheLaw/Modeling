const val xF = 10
const val picF = 120
const val picFfo = picF / 4 - 1
const val eilF = 40
const val rkF = 40
const val wholetable = xF + picF + picFfo + eilF + rkF

fun printHead()
{
    print(String.format("|%-${xF}s|%-${picF - 1}s|%-${eilF}s|%-${rkF}s|\n", "", "", "", "").replace(' ', '-'))
    print(String.format("|%-${xF}s|%-${picF - 1}s|%-${eilF}s|%-${rkF}s|\n", "x", "Метод Пикара", "Метод Эйлера", "Метод Рунге-Кутта 2-го порядка точности"))
    print(String.format("|%-${xF}s|%-${picF - 1}s|%-${eilF}s|%-${rkF}s|\n", "", "", "", "").replace(' ', '-'))

    print(String.format("|%${xF}s|", " "))
    for (i in 1..4)
        print(String.format("%-${picFfo}s|", "Приближение $i"))
    print(String.format("%${eilF}s|%${rkF}s|", "", ""))
}

fun main()
{
    printHead()
}