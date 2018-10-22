package com.speakeasy.watsonbarassistant.Discovery

import com.ibm.watson.developer_cloud.discovery.v1.model.QueryResponse
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.MainMenu
import junit.framework.TestCase
import kotlinx.serialization.stringFromUtf8Bytes
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations

class SearchDiscoveryTest: TestCase() {

    lateinit var searchDiscovery: SearchDiscovery
    @Mock
    lateinit var handleDiscovery: HandleDiscovery
    @Mock
    lateinit var queryResponse: QueryResponse

    @Before
    override fun setUp(){
        MockitoAnnotations.initMocks(this)

        searchDiscovery = SearchDiscovery(handleDiscovery)
    }

    @Test
    fun testCreateAndSetsBasic() {
        var ingredients = mutableListOf<Ingredient>()
        ingredients.add(Ingredient("a"))

        Assert.assertEquals("[[a]]", searchDiscovery.createAndSets(ingredients.toTypedArray(), 0, 0).toString())
    }

    @Test
    fun testCreateAndSetsMulti() {
        var ingredients = mutableListOf<Ingredient>()
        var expected = mutableListOf<MutableSet<String>>()
        var set1 = mutableSetOf<String>()
        var set2 = mutableSetOf<String>()
        var set3 = mutableSetOf<String>()

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
        var ingredients = mutableListOf<Ingredient>()
        var expected = mutableListOf<MutableSet<String>>()
        var set1 = mutableSetOf<String>()
        var set2 = mutableSetOf<String>()
        var set3 = mutableSetOf<String>()

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
        var ingredients = mutableListOf<Ingredient>()
        var expected = mutableListOf<MutableSet<String>>()
        var set1 = mutableSetOf<String>()

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
        var input = mutableListOf<MutableSet<String>>()
        var expected = "(a)"
        var subSet1 = mutableSetOf<String>()

        subSet1.add("a")

        input.add(subSet1)

        Assert.assertEquals(expected, searchDiscovery.buildAndString(input))
    }

    @Test
    fun testBuildAndStringTwo() {
        var input = mutableListOf<MutableSet<String>>()
        var expected = "(a,b)|(c)"
        var subSet1 = mutableSetOf<String>()
        var subSet2 = mutableSetOf<String>()

        subSet1.add("a")
        subSet1.add("b")

        subSet2.add("c")

        input.add(subSet1)
        input.add(subSet2)

        Assert.assertEquals(expected, searchDiscovery.buildAndString(input))
    }

    @Test
    fun testBuildAndStringMany() {
        var input = mutableListOf<MutableSet<String>>()
        var expected = "(a,b)|(c)|(d,e,f)"
        var subSet1 = mutableSetOf<String>()
        var subSet2 = mutableSetOf<String>()
        var subSet3 = mutableSetOf<String>()

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
        var input = mutableListOf<MutableSet<String>>()
        var expected = ""

        Assert.assertEquals(expected, searchDiscovery.buildAndString(input))
    }

    @Test
    fun testBuildAndStringZeroMany() {
        var input = mutableListOf<MutableSet<String>>()
        var expected = "()|()"
        var subSet = mutableSetOf<String>()

        input.add(subSet)
        input.add(subSet)

        Assert.assertEquals(expected, searchDiscovery.buildAndString(input))
    }
}