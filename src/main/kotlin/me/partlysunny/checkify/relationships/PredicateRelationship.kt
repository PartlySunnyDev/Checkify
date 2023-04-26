package me.partlysunny.checkify.relationships

import me.partlysunny.checkify.CheckerPredicate
import me.partlysunny.checkify.PredicateContext

abstract class PredicateRelationship protected constructor(
    protected val a: CheckerPredicate?,
    protected val b: CheckerPredicate?
) {
    abstract fun check(ctx: PredicateContext): Boolean
}
