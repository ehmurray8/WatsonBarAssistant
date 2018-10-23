package com.speakeasy.watsonbarassistant.discovery

import com.speakeasy.watsonbarassistant.Ingredient
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SearchDiscoveryTest: TestCase() {

    private lateinit var searchDiscovery: SearchDiscovery
    @Mock
    lateinit var handleDiscovery: HandleDiscovery

    @Before
    override fun setUp(){
        MockitoAnnotations.initMocks(this)

        searchDiscovery = SearchDiscovery(handleDiscovery)
    }

    @Test
    fun testCreateAndSetsBasic() {
        val ingredients = mutableListOf<Ingredient>()
        ingredients.add(Ingredient("a"))

        Assert.assertEquals("[[a]]", searchDiscovery.createAndSets(ingredients.toTypedArray(), 0, 0).toString())
    }

    @Test
    fun testCreateAndSetsMulti() {
        val ingredients = mutableListOf<Ingredient>()
        val expected = mutableListOf<MutableSet<String>>()
        val set1 = mutableSetOf<String>()
        val set2 = mutableSetOf<String>()
        val set3 = mutableSetOf<String>()

        ingredients.add(Ingredient("a"))
        ingredients.add(Ingredient("b"))
        ingredients.add(Ingredient("c"))

        set1.add("a")
        set2.add("b")
        set3.add("c")

        expected.add(set1)
        expected.add(set2)
        expected.add(set3)

        Assert.assertEquals(expected, searchDiscovery.createAndSets(ingredients.toTypedArray(), 0, 0))
    }

    @Test
    fun testCreateAndSetsMultiSizeTwo() {
        val ingredients = mutableListOf<Ingredient>()
        val expected = mutableListOf<MutableSet<String>>()
        val set1 = mutableSetOf<String>()
        val set2 = mutableSetOf<String>()
        val set3 = mutableSetOf<String>()

        ingredients.add(Ingredient("a"))
        ingredients.add(Ingredient("b"))
        ingredients.add(Ingredient("c"))

        set1.add("a")
        set1.add("b")

        set2.add("a")
        set2.add("c")

        set3.add("b")
        set3.add("c")

        expected.add(set1)
        expected.add(set2)
        expected.add(set3)

        Assert.assertEquals(expected, searchDiscovery.createAndSets(ingredients.toTypedArray(), 0, 1))
    }

    @Test
    fun testCreateAndSetsMultiSizeThree() {
        val ingredients = mutableListOf<Ingredient>()
        val expected = mutableListOf<MutableSet<String>>()
        val set1 = mutableSetOf<String>()

        ingredients.add(Ingredient("a"))
        ingredients.add(Ingredient("b"))
        ingredients.add(Ingredient("c"))

        set1.add("a")
        set1.add("b")
        set1.add("c")

        expected.add(set1)

        Assert.assertEquals(expected, searchDiscovery.createAndSets(ingredients.toTypedArray(), 0, 2))
    }

    @Test
    fun testBuildAndString() {
        val input = mutableListOf<MutableSet<String>>()
        val expected = "(a)"
        val subSet1 = mutableSetOf<String>()

        subSet1.add("a")

        input.add(subSet1)

        Assert.assertEquals(expected, searchDiscovery.buildAndString(input))
    }

    @Test
    fun testBuildAndStringTwo() {
        val input = mutableListOf<MutableSet<String>>()
        val expected = "(a,b)|(c)"
        val subSet1 = mutableSetOf<String>()
        val subSet2 = mutableSetOf<String>()

        subSet1.add("a")
        subSet1.add("b")

        subSet2.add("c")

        input.add(subSet1)
        input.add(subSet2)

        Assert.assertEquals(expected, searchDiscovery.buildAndString(input))
    }

    @Test
    fun testBuildAndStringMany() {
        val input = mutableListOf<MutableSet<String>>()
        val expected = "(a,b)|(c)|(d,e,f)"
        val subSet1 = mutableSetOf<String>()
        val subSet2 = mutableSetOf<String>()
        val subSet3 = mutableSetOf<String>()

        subSet1.add("a")
        subSet1.add("b")

        subSet2.add("c")

        subSet3.add("d")
        subSet3.add("e")
        subSet3.add("f")

        input.add(subSet1)
        input.add(subSet2)
        input.add(subSet3)

        Assert.assertEquals(expected, searchDiscovery.buildAndString(input))
    }

    @Test
    fun testBuildAndStringZero() {
        val input = mutableListOf<MutableSet<String>>()
        val expected = ""

        Assert.assertEquals(expected, searchDiscovery.buildAndString(input))
    }

    @Test
    fun testBuildAndStringZeroMany() {
        val input = mutableListOf<MutableSet<String>>()
        val expected = "()|()"
        val subSet = mutableSetOf<String>()

        input.add(subSet)
        input.add(subSet)

        Assert.assertEquals(expected, searchDiscovery.buildAndString(input))
    }
}