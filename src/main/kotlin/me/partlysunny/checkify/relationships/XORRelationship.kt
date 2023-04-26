package me.partlysunny.checkify.relationships

import me.partlysunny.checkify.CheckerPredicate
import me.partlysunny.checkify.PredicateContext

class XORRelationship(a: CheckerPredicate?, b: CheckerPredicate?) : PredicateRelationship(a, b) {
    override fun check(ctx: PredicateContext): Boolean {
        return a!!.process(ctx) xor b!!.process(ctx)
    }
}
