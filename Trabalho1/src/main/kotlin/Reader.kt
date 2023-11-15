data class Point(var tterm: String, var pos: Int?)

fun read( terms: List<String>, index: Int): String {

    val t = terms
    val i = index
    val r: String


    if (t.size == 1) {
        when {
            t[i] == "true" || t[i] == "false" -> "Bool"
            t[i].toIntOrNull() != null -> {
                if (t[i].toInt() < 0) "-" else "Nat"
            }

            t[i] == "suc" -> "( Nat -> Nat )"
            t[i] == "pred" -> "( Nat -> Nat )"
            t[i] == "ehzero" -> "( Nat -> Bool )"
            else -> "-"
        }
    } else {
        when (t[i]) {
            "true" ->
                "true"

            "false" ->
                "false"

            "if" ->
                if (readType(t, i + 1, false) == "Bool")
            /*"then" ->
            ...
            "else" ->
            ...
            "endif" ->
            ...*/
            else -> "-"
        }
    }
    return "Bool"
}

fun readType(terms: List<String>, index: Int, flag: Boolean): String {

    val t = terms
    val i = index
    val f = flag
    var r = Point("",i)

    if (!f) {

        when {
            t[i] == "(" ->
                readType(t, i + 1, true)
            t[i] == "true" || t[i] == "false" -> "Bool"

            t[i].toIntOrNull() != null -> {
                if (t[i].toInt() < 0) "-" else "Nat"
            }
            t[i] == "suc" || t[i] == "pred" -> "( Nat -> Nat )"

            t[i] == "ehzero" -> "( Nat -> Bool )"

            else -> "-"
        }

    } else {
        when {
            t[i] == "ehzero" -> {
                r.tterm = readType(t, i + 1, false)
                if (r.tterm == "Nat")
                    "Bool"
                else "-"
            }
            t[i] == "suc" || t[i] == "pred"-> {
                r.tterm = readType(t, i+1, false)
                if (r.tterm == "Nat")
                    "Nat"
                else "-"
            }

        }
    }
    return "non"
}


fun readIf(terms: List<String>, index: Int): Point {
    val t = terms
    val i = index
    var r = Point("", null)

    if ((t[i] != "endif") || t[i] != "if") {
        r = readIf(t, i + 1)

        when {
            t[i] == ")" || t[i] == "("->
                r

            t[i].toIntOrNull() != null -> {
                if ((r.tterm == "Nat") || (r.tterm == "")) {
                    if (t[i].toInt() < 0) {
                        r.tterm = "-";
                    }
                } else {
                    r.tterm = "-";
                }
                return r
            }

            (t[i] == "suc" || t[i] == "pred") && r.tterm == "Nat" ->
                return r

            (t[i] == "suc" || t[i] == "pred") && r.tterm == "" -> {
                r.tterm = "Nat -> Nat";
                return r
            }

            t[i] == "else" ->
                return r

            t[i] == "then" -> {;
                return r
            }
        }
    } else if (t[i] == "if"){
        r = readIf(t, i)


    } else {
        r.pos = i
        return r
    }
    return r
}

