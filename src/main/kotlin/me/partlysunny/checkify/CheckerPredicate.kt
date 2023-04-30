package me.partlysunny.checkify

import me.partlysunny.checkify.relationships.PredicateRelationship
import me.partlysunny.checkify.relationships.Relationship
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.function.BiFunction
import java.util.stream.Collectors

class CheckerPredicate(private val predicate: String) {
    private val chunks: List<String>

    init {
        chunks = chunk()
        checkValid()
    }

    private fun checkValid() {
        //Check brackets
        assert(isBracketReady) { "Predicate $predicate is not bracket ready!" }
        //Check chunks
        assert(chunks.size % 2 == 1) { "Invalid predicate $predicate, chunks must have odd length: Chunks are: $chunks" }
        for (i in chunks.indices) {
            val chunk = chunks[i]
            if (i % 2 == 1) {
                try {
                    Relationship.valueOf(chunk)
                } catch (e: Exception) {
                    throw IllegalArgumentException(
                        "All predicate joiners must be %s, Found: %s".formatted(
                            Arrays.toString(
                                Relationship.values()
                            ), chunk
                        )
                    )
                }
            }
        }
    }

    private val isBracketReady: Boolean
        get() {
            val stack = Stack<Boolean>()
            for (i in predicate.indices) {
                if (predicate[i] == '(') {
                    stack.push(true)
                } else if (predicate[i] == ')') {
                    if (stack.empty()) return false
                    stack.pop()
                }
            }
            return stack.empty()
        }

    fun chunk(): List<String> {
        var chunks: MutableList<String> = ArrayList()
        val brackets = Stack<Int>()
        var start = 0
        var i = 0
        while (i < predicate.length) {
            val c = predicate[i]
            if (c == '(') {
                brackets.push(i)
            } else if (c == ')') {
                val startBracket = brackets.pop()
                if (brackets.empty()) {
                    chunks.add(predicate.substring(startBracket + 1, i))
                    start = i + 2
                }
            }
            for (r in Relationship.values()) {
                val name = r.toString()
                if (c == name[0] && i < predicate.length - name.length && i > 0) {
                    val next = predicate.substring(i - 1, i + name.length + 1)
                    if (next == " $name ") {
                        if (!brackets.empty()) {
                            continue
                        }
                        if (start != i) chunks.add(predicate.substring(start, i))
                        chunks.add(next)
                        start = i + name.length
                        i += name.length - 1
                    }
                }
            }
            i++
        }
        if (start < predicate.length) {
            if (!brackets.empty()) {
                chunks.add(predicate.substring(brackets.pop()))
            } else {
                chunks.add(predicate.substring(start))
            }
        }
        chunks = chunks.stream().map { obj: String -> obj.trim { it <= ' ' } }.collect(Collectors.toList())
        return chunks
    }

    private fun processTermWithContext(ctx: PredicateContext, term: String): String? {
        if (term.length == 0) return term
        return if (term.startsWith("?")) {
            ctx[term.substring(1)]
        } else term
    }

    fun process(ctx: PredicateContext): Boolean {
        if (chunks.size == 1) {
            val expression = chunks[0]
            //Process the one chunk
            //"true" or "false"
            if (expression == "true") {
                return true
            } else if (expression == "false") {
                return false
            }
            val expressionItems = expression.split(" ++".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (expressionItems.size == 3) {
                val term1 = processTermWithContext(ctx, expressionItems[0])
                val operator = expressionItems[1]
                val term2 = processTermWithContext(ctx, expressionItems[2])
                when (operator) {
                    "=" -> {
                        var stringEquals = term1 == term2
                        if (!stringEquals) {
                            try {
                                stringEquals = term1!!.toDouble() == term2!!.toDouble()
                            } catch (ignored: NumberFormatException) {
                            }
                        }
                        return stringEquals
                    }

                    "!=" -> {
                        var stringEquals = term1 != term2
                        if (!stringEquals) {
                            try {
                                stringEquals = term1!!.toDouble() != term2!!.toDouble()
                            } catch (ignored: NumberFormatException) {
                            }
                        }
                        return stringEquals
                    }

                    ">" -> {
                        return try {
                            term1!!.toDouble() > term2!!.toDouble()
                        } catch (ignored: NumberFormatException) {
                            throw IllegalArgumentException(
                                "Left and right terms must both be parsable doubles in expression %s".formatted(
                                    expression
                                )
                            )
                        }
                    }

                    "<" -> {
                        return try {
                            term1!!.toDouble() < term2!!.toDouble()
                        } catch (ignored: NumberFormatException) {
                            throw IllegalArgumentException(
                                "Left and right terms must both be parsable doubles in expression %s".formatted(
                                    expression
                                )
                            )
                        }
                    }

                    ">=" -> {
                        return try {
                            term1!!.toDouble() >= term2!!.toDouble()
                        } catch (ignored: NumberFormatException) {
                            throw IllegalArgumentException(
                                "Left and right terms must both be parsable doubles in expression %s".formatted(
                                    expression
                                )
                            )
                        }
                    }

                    "<=" -> {
                        return try {
                            term1!!.toDouble() <= term2!!.toDouble()
                        } catch (ignored: NumberFormatException) {
                            throw IllegalArgumentException(
                                "Left and right terms must both be parsable doubles in expression %s".formatted(
                                    expression
                                )
                            )
                        }
                    }
                }
            }
            throw IllegalArgumentException("Invalid expression found in predicate! %s".formatted(expression))
        }
        var endRelationship: PredicateRelationship? = null
        for (i in chunks.indices) {
            val item = chunks[i]
            //If the variable "i" is even then it is an operator, otherwise it's an expression
            if (i % 2 == 1) {
                val operationProcessor =
                    BiFunction<CheckerPredicate, CheckerPredicate, PredicateRelationship> { a: CheckerPredicate?, b: CheckerPredicate? ->
                        for (r in Relationship.values()) {
                            if (item == r.toString()) {
                                try {
                                    return@BiFunction r.clazz().getDeclaredConstructor(
                                        CheckerPredicate::class.java,
                                        CheckerPredicate::class.java
                                    ).newInstance(a, b)
                                } catch (e: InstantiationException) {
                                    throw RuntimeException(e)
                                } catch (e: IllegalAccessException) {
                                    throw RuntimeException(e)
                                } catch (e: InvocationTargetException) {
                                    throw RuntimeException(e)
                                } catch (e: NoSuchMethodException) {
                                    throw RuntimeException(e)
                                }
                            }
                        }
                        throw IllegalStateException("Somehow the program really broke down somewhere, better contact devs")
                    }
                endRelationship = if (endRelationship == null) {
                    operationProcessor.apply(CheckerPredicate(chunks[i - 1]), CheckerPredicate(chunks[i + 1]))
                } else {
                    operationProcessor.apply(from(endRelationship.check(ctx)), CheckerPredicate(chunks[i + 1]))
                }
            }
        }
        assert(endRelationship != null)
        return endRelationship!!.check(ctx)
    }

    companion object {
        private val TRUE = CheckerPredicate("true")
        private val FALSE = CheckerPredicate("false")
        fun from(b: Boolean): CheckerPredicate {
            return if (b) TRUE else FALSE
        }
    }
}
