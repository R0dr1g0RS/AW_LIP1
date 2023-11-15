data class Context(val box: String, val type: String)

fun t (terms: List<String>, indexIni: Int, indexFin: Int, context: MutableList<Context>?): String {

    if (indexFin >= indexIni) {
        return when (terms[indexIni]) {
            "true" -> tTrue(terms, indexIni, indexFin)
            "false" -> tFalse(terms, indexIni, indexFin)
            "if" -> {
                val endif = findPairs(terms, indexIni, indexFin, "if", "endif")
                if (endif != null) {
                    tIf(terms, indexIni, endif, context)
                } else {
                    "!"
                }
            }

            "suc" -> tSuc(terms, indexIni, indexFin)
            "pred" -> tPred(terms, indexIni, indexFin)
            "ehzero" -> tEhzero(terms, indexIni, indexFin)
            "(" -> tApl(terms, indexIni, indexFin, context)
            "lambda" -> tAbs(terms, indexIni, indexFin, context)
            else ->
                if (terms[indexIni].toIntOrNull() != null) {
                    tNat(terms, indexIni, indexFin)
                } else {
                    tVar(terms, indexIni, indexFin, context)
                }
        }
    }
    return ("!");
}

fun tVar(terms: List<String>, indexIni: Int, indexFin: Int, context: MutableList<Context>?): String {
    if (context != null) {
        for (i in context.size downTo 0) {
            if(terms[indexIni] == context[i].box)
                return context[i].type
        }
        return "-"

    } else {
        return "-"
    }
}

fun tAbs(terms: List<String>, indexIni: Int, indexFin: Int, context: MutableList<Context>?): String {
    val endLambda = findPairs(terms, indexIni, indexFin, "lambda", "end")
    val gama = context

    // if the variable starts with a number
    val varia = terms[indexIni+1].substring(0,1)
    if (varia.toIntOrNull() != null)
        return "!"


    try {
        val endType = findPairs(terms, indexIni, indexFin, "lambda", ".")
        println(endType)
        var xU = ""
        for (i in indexIni+3..checkNotNull(endType)) {
            println(i)

            xU += terms[i]
            if (i < endType)
                xU += " "
        }
        val c = Context(terms[indexIni+2], xU)
        gama?.add(c)
        println(endType+2)
        val t = t(terms, endType+2, checkNotNull(endLambda)-1, gama)
        gama?.remove(c)

        return ("( $xU -> $t )")

    } catch (ex: NullPointerException) {
        println("T_Abs - NullPointer Exception: ${ex.message}")
        return ("!")
    } catch (ex: Exception) {
        println("T_Abs - Another Exception: ${ex.message}")
        return ("!")
    }

}

fun tApl(terms: List<String>, indexIni: Int, indexFin: Int, context: MutableList<Context>?): String {
    val endParen = findPairs(terms, indexIni, indexFin, "(", ")")
    try {
        val t = t(terms, indexIni+1, checkNotNull(endParen)-1, context)
        val u = when (terms[indexFin-1]){
            ")" -> {
                val startU = findPairs(terms, indexFin - 1, indexIni + 1, ")", "(")
                t(terms, checkNotNull(startU), indexFin-1, context)
            }
            "endif" -> {
                val startU = findPairs(terms, indexFin - 1, indexIni + 1, "endif", "if")
                t(terms, checkNotNull(startU), indexFin-1, context)
            }
            "end" -> {
                val startU = findPairs(terms, indexFin - 1, indexIni + 1, "end", "lambda")
                t(terms, checkNotNull(startU), indexFin-1, context)
            }
            else -> t(terms, indexFin-1, indexFin-1, context)
        }

        // Ex.: t = '( Nat -> Nat )' and u = 'Nat'
        // if t starts with '( Nat', then return '( Nat )', cutting out '( Nat ->' and adding '( '
        if (t.startsWith("( $u")){
            val result = "( " + t.substring(u.length+5, t.length)
            // "P.O.G." below:
            if (result.length == 9)
                return "Bool"
            if (result.length == 8)
                return "Nat"

            return (result)
        }

        // Ex.: t = '( Nat -> Nat ) -> Nat' and u = '( Nat -> Nat )'
        // if t starts with '( Nat -> Nat )', then return '( Nat )', cutting out '( Nat -> Nat ) ->' and adding '( '
        if (t.startsWith(u)){
            val result = "( " + t.substring(u.length+2, t.length)
            // "P.O.G." below:
            if (result.length == 9){
                println("|" + result + "|")
                return "Bool"}
            if (result.length == 8)
                return "Nat"

            return (result)
        }

        return "!"

    } catch (ex: NullPointerException) {
        println("T_Apl - NullPointer Exception: ${ex.message}")
        return ("!")
    } catch (ex: Exception) {
        println("T_Apl - Another Exception: ${ex.message}")
        return ("!")
    }
}

fun tNat(terms: List<String>, indexIni: Int, indexFin: Int): String {
    if (indexFin == indexIni) {
        if (terms[indexIni].toInt() >= 0) {
            return "Nat"
        }
    }
    return "!"
}

fun tEhzero(terms: List<String>, indexIni: Int, indexFin: Int): String {
    return "Nat -> Bool"
}

fun tPred(terms: List<String>, indexIni: Int, indexFin: Int): String {
    return "( Nat -> Nat )"
}

fun tSuc(terms: List<String>, indexIni: Int, indexFin: Int): String {
    return "( Nat -> Nat )"
}

fun tTrue(terms: List<String>, indexIni: Int, indexFin: Int): String {
    if (indexFin == indexIni){
        return "Bool"
    }
    return  ("!")
}

fun tFalse(terms: List<String>, indexIni: Int, indexFin: Int): String {
    if (indexFin == indexIni) {
        return "Bool"
    }
    return ("!")
}

fun tIf(terms: List<String>, indexIni: Int, indexFin: Int, context: MutableList<Context>?): String {
    try {
        val pThen = findPairs(terms, indexIni, indexFin, "if", "then")
        val pElse = findPairs(terms, checkNotNull(pThen), indexFin, "then", "else")
        val t2 = t(terms, pThen+1, checkNotNull(pElse)-1, context)
        val t1 = t(terms, indexIni+1, pThen -1, context)
        val t3 = t(terms, pElse+1, indexFin-1, context)

        if ((t1 == "Bool") and (t2 == t3)){
            return t2
        }

        return "!"

    } catch (ex: NullPointerException) {
        println("T_If - NullPointer Exception: ${ex.message}")
        return ("!")
    } catch (ex: Exception) {
        println("T_If - Another Exception: ${ex.message}")
        return ("!")
    }
}

fun findPairs (terms: List<String>, indexIni: Int, indexFin: Int, iniE: String, endE: String): Int? {

    var countP1 = 0
    var countP2 = 0
    if (indexFin >= indexIni) {
        for (i in indexIni..indexFin) {

            if (terms[i] == iniE) {
                countP1++
            }

            if (terms[i] == endE) {
                countP2++
                if (countP1 > 0) {
                    if (countP1 == 1)
                        return i
                    countP1--
                    countP2--
                }
            }
        }
    } else {
        for (i in indexIni downTo indexFin) {

            if (terms[i] == iniE) {
                countP1++
            }

            if (terms[i] == endE) {
                countP2++
                if (countP1 > 0) {
                    if (countP1 == 1)
                        return i
                    countP1--
                    countP2--
                }
            }
        }
    }
    return null
}

fun main() {
    println("Digite: ")
    val input = readlnOrNull()
    if (input != null){
        val term = input.split(" ")
        val gama: MutableList<Context>? = null
        println(t(term, 0, term.size-1, gama))
    } else return println("!")
}
