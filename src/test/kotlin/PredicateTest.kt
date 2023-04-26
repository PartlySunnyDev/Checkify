import me.partlysunny.checkify.CheckerPredicate
import me.partlysunny.checkify.PredicateContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions as Assert

class PredicateTest {
    @Test
    fun chunkTest() {
        Assert.assertEquals(
            listOf("(a AND b) OR c", "AND", "d", "OR", "e"),
            CheckerPredicate("((a AND b) OR c) AND d OR e").chunk()
        )
        Assert.assertEquals(
            listOf("a AND b", "OR", "e", "AND", "?weather is SUNNY", "OR", "d AND c"),
            CheckerPredicate("(a AND b) OR e AND ?weather is SUNNY OR (d AND c)").chunk()
        )
        Assert.assertEquals(
            listOf("a AND b", "OR", "e", "AND", "?weather is STORMY", "AND", "d AND c"),
            CheckerPredicate("(a AND b) OR e AND ?weather is STORMY AND (d AND c)").chunk()
        )
    }

    @Test
    fun relationshipTest() {
        Assert.assertTrue(
            CheckerPredicate("(true XOR false) OR ((true AND false) XNOR (false OR false))").process(
                PredicateContext()
            )
        )
    }

    @Test
    fun expressionTest() {
        Assert.assertTrue(
            CheckerPredicate("?weather = SUNNY").process(
                PredicateContext(
                    HashMapBuilder<String?, String?>().put(
                        "weather",
                        "SUNNY"
                    ).build()
                )
            )
        )
        Assert.assertTrue(
            CheckerPredicate("(?weather = SUNNY AND ?time > 400) OR (?weather = STORMY)").process(
                PredicateContext(
                    HashMapBuilder<String?, String?>().put("weather", "STORMY").put("time", "300").build()
                )
            )
        )
        Assert.assertFalse(
            CheckerPredicate("(?weather = SUNNY AND ?time > 400) OR (?weather = STORMY)").process(
                PredicateContext(
                    HashMapBuilder<String?, String?>().put("weather", "SUNNY").put("time", "200").build()
                )
            )
        )
    }

    class HashMapBuilder<T, U> {

        private val map: HashMap<T, U> = HashMap()

        fun put(key: T, value: U): HashMapBuilder<T, U> {
            map[key] = value
            return this
        }

        fun build(): HashMap<T, U> {
            return map
        }

    }
}