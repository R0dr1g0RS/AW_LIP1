import kotlin.system.exitProcess
data class Context(val box: String, val type: String)

fun t (terms: List<String>, indexIni: Int, indexFin: Int, context: MutableList<Context>?): String {
    //println("ping")
    if (indexFin >= indexIni) {
        //println(indexIni.toString() + " -> " + terms[indexIni])
        val result = when (terms[indexIni]) {
            "true" -> tTrue(indexIni, indexFin)
            "false" -> tFalse(indexIni, indexFin)
            "if" -> tIf(terms, indexIni, indexFin, context)
            "suc" -> tSuc(terms, indexIni, indexFin)
            "pred" -> tPred(terms, indexIni, indexFin)
            "ehzero" -> tEhzero(terms, indexIni, indexFin)
            "(" -> tApl(terms, indexIni, indexFin, context)
            "lambda" -> tAbs(terms, indexIni, indexFin, context)
            else ->
                if (terms[indexIni].toIntOrNull() != null) {
                    tNat(terms, indexIni, indexFin)
                } else {
                    //println("pingVar")
                    //println(indexIni.toString() + "_" + terms[indexIni])
                    tVar(terms, indexIni, context)
                }
        }
        if ((result == "!") || (result == "-")) {
            println(result)
            @Suppress("UNREACHABLE_CODE")
            return exitProcess(0)
        }
        return (result)
    }
    //println(indexIni.toString() + "_" + terms[indexIni])
    return ("!")
}

fun tVar(terms: List<String>, indexIni: Int, context: MutableList<Context>?): String {
    val values = listOf("if", "endif", "else", "then", "lambda", "end")
    if (context != null) {
        //println("CONTEXTO:")
        for (i in context.size-1 downTo 0 ) {
            //println(context[i].box + " - " + context[i].type)

            if(terms[indexIni] == context[i].box)
                return context[i].type
        }
        return ("-")
        //@Suppress("UNREACHABLE_CODE")
        //return exitProcess(0)

    }

    if (terms[indexIni] in values) {
        return ("!")
    }
    return ("-")
    //@Suppress("UNREACHABLE_CODE")
    //return exitProcess(0)

}

fun tAbs(terms: List<String>, indexIni: Int, indexFin: Int, context: MutableList<Context>?): String {
    if (terms[indexFin] != "end" ){
        return "!"
    }
    try {
        //val endLambda = findPairs(terms, indexIni, indexFin, "lambda", "end")
        //println(endLambda)
        var gama = context

        // if the variable starts with a number
        val varia = terms[indexIni+1].substring(0,1)
        if (varia.toIntOrNull() != null)
            return "!"


        val endType = findPairs(terms, indexIni, indexFin, "lambda", ".")
        //println(endType)
        var xU = ""

        for (i in indexIni+3..(checkNotNull(endType)-1)) {
            //println(i)

            xU += terms[i]
            if (i < endType-1)
                xU += " "
        }
        //println(xU)
        val c = Context(terms[indexIni+1], xU)
        if (gama == null) {
            //println(c.box + " - " + c.type)
            gama = mutableListOf(c)
        } else {
            //println(c.box + " - " + c.type)
            gama.add(c)
        }
        //println(endType)
        val t = t(terms, endType+1, indexFin-1, gama)
        gama.remove(c)

        //println("( $xU -> $t )")
        return ("( $xU -> $t )")

    } catch (ex: NullPointerException) {
        //println("T_Abs - NullPointer Exception: ${ex.message}")
        return ("!")
    } catch (ex: Exception) {
        //println("T_Abs - Another Exception: ${ex.message}")
        return ("!")
    }

}

fun tApl(terms: List<String>, indexIni: Int, indexFin: Int, context: MutableList<Context>?): String {
    val endParen = findPairs(terms, indexIni, indexFin, "(", ")")
    try {
        //println(checkNotNull(endParen))
        val t = when (terms[indexIni+1]) {
            "(" -> {
                val closeT = findPairs(terms, indexIni + 1, indexFin - 1, "(", ")")
                t(terms, indexIni + 1, checkNotNull(closeT), context)
            }

            "if" -> {
                val closeT = findPairs(terms, indexIni + 1, indexFin - 1, "if", "endif")
                t(terms, indexIni + 1, checkNotNull(closeT), context)
            }

            "lambda" -> {
                val closeT = findPairs(terms, indexIni + 1, indexFin - 1, "lambda", "end")
                t(terms, indexIni + 1, checkNotNull(closeT), context)
            }

            else -> {
                //println(indexFin.toString()+" Apl" + terms[indexFin])
                //println(terms[indexFin-1])
                t(terms, indexIni + 1, indexIni + 1, context)
            }
        }
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
            else -> {
                //println(indexFin.toString()+" Apl" + terms[indexFin])
                //println(terms[indexFin-1])
                t(terms, indexFin-1, indexFin-1, context)
            }
        }

        // Ex.: t = '( Nat -> Nat )' and u = 'Nat'
        // if t starts with '( Nat', then return '( Nat )', cutting out '( Nat ->' and adding '( '
        //println("...$t...$u...")
        if (t.startsWith("( $u")){
            val result = "( " + t.substring(u.length+6, t.length)
            //println(result)

            if (result == "( Bool )")
                return "Bool"
            if (result == "( Nat )") {
                //println("é o caso")
                return "Nat"
            }

            return (result)
        }

        // Ex.: t = '( Nat -> Nat ) -> Nat' and u = '( Nat -> Nat )'
        // if t starts with '( Nat -> Nat )', then return '( Nat )', cutting out '( Nat -> Nat ) ->' and adding '( '
        if (t.startsWith(u.substring(0, u.length-1))){
            //println("...$t...$u...")
            val result = "( " + t.substring(u.length+4, t.length)

            if (result == "( Bool )")
                return "Bool"
            if (result == "( Nat )")
                return "Nat"

            return (result)
        }

        return "-"

    } catch (ex: NullPointerException) {
        //println("T_Apl - NullPointer Exception: ${ex.message}")
        return ("!")
    } catch (ex: Exception) {
        //println("T_Apl - Another Exception: ${ex.message}")
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
    if (indexFin == indexIni)
        return "( Nat -> Bool )"
    try {
        if (terms[indexIni - 1] == "(")
            return "( Nat -> Bool )"
        return ("!")
    } catch (ex: Exception) {
        //println("T_Suc - Another Exception: ${ex.message}")
        return ("!")
    }
}

fun tPred(terms: List<String>, indexIni: Int, indexFin: Int): String {
    if (indexFin == indexIni)
        return "( Nat -> Nat )"
    try {
        if (terms[indexIni - 1] == "(")
            return "( Nat -> Nat )"
        return ("!")
    } catch (ex: Exception) {
        //println("T_Suc - Another Exception: ${ex.message}")
        return ("!")
    }
}

fun tSuc(terms: List<String>, indexIni: Int, indexFin: Int): String {
    if (indexFin == indexIni)
        return "( Nat -> Nat )"
    try {
        if (terms[indexIni - 1] == "(")
            return "( Nat -> Nat )"
        return ("!")
    } catch (ex: Exception) {
        //println("T_Suc - Another Exception: ${ex.message}")
        return ("!")
    }
}

fun tTrue(indexIni: Int, indexFin: Int): String {
    if (indexFin == indexIni){
        return "Bool"
    }
    return  ("!")
}

fun tFalse(indexIni: Int, indexFin: Int): String {
    if (indexFin == indexIni) {
        return "Bool"
    }
    return ("!")
}

fun tIf(terms: List<String>, indexIni: Int, indexFin: Int, context: MutableList<Context>?): String {
    val endif = findPairs(terms, indexIni, indexFin, "if", "endif")
    //println(endif)
    if ((endif == null) || (endif != indexFin)){
        return "!"
    } else {
        try {
            val pThen = findPairs(terms, indexIni, indexFin, "if", "then")
            checkNotNull(pThen)
            val pElse = findPairs(terms, pThen, indexFin, "then", "else")
            val t2 = t(terms, pThen.plus(1), checkNotNull(pElse) - 1, context)
            val t1 = t(terms, indexIni + 1, pThen.minus(1), context)
            val t3 = t(terms, pElse + 1, indexFin - 1, context)
            //println("...$t1...$t2...$t3...")
            if ((t1 == "Bool") and (t2 == t3)) {
                return t2
            }

            return "!"

        } catch (ex: NullPointerException) {
            //println("T_If - NullPointer Exception: ${ex.message}")
            return ("!")
        } catch (ex: Exception) {
            //println("T_If - Another Exception: ${ex.message}")
            return ("!")
        }
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
    try {
        //println("Digite: ")
        val input: String? = readLine()
        //val input = "( lambda x : ( Nat -> Nat ) . ( lambda x : Nat . x end ( x 1 ) ) end pred )"
        //val input = "lambda f : Nat . if ( ehzero f ) then ( suc f ) else ( pred f ) endif end"
        //println(input)
        val term = input?.split(" ")
        val gama: MutableList<Context>? = null
        val result = t((term ?: listOf("Error")), 0, (term?.size ?: 0) -1, gama)
        println(result)
    } catch (ex: NullPointerException) {
        //println("T_Apl - NullPointer Exception: ${ex.message}")
        println("!")
    } catch (ex: Exception) {
        //println("T_Apl - Another Exception: ${ex.message}")
        println("!")
    }
}
