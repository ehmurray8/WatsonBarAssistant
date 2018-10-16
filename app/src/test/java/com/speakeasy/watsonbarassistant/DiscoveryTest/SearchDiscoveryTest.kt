package com.speakeasy.watsonbarassistant.DiscoveryTest

import com.ibm.watson.developer_cloud.discovery.v1.model.QueryResponse
import com.ibm.watson.developer_cloud.discovery.v1.model.QueryResult
import com.speakeasy.watsonbarassistant.Discovery.HandleDiscovery
import com.speakeasy.watsonbarassistant.Discovery.SearchDiscovery
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.Recipe.DiscoveryRecipe
import junit.framework.TestCase
import kotlinx.serialization.json.JSON
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class SearchDiscoveryTest: TestCase() {

    lateinit var searchDiscovery: SearchDiscovery
    @Mock lateinit var handleDiscovery: HandleDiscovery
    @Mock lateinit var queryResponse: QueryResponse

    @Before
    override fun setUp(){
        MockitoAnnotations.initMocks(this)

        searchDiscovery = SearchDiscovery(handleDiscovery)
    }

    @Test
    fun testBuildIngredientQueryTypical(){

        var ingredients = mutableListOf<Ingredient>()

        ingredients.add(0, Ingredient("Item1"))
        ingredients.add(1, Ingredient("ItemTwo"))

        Assert.assertEquals("ingredientList:Item1|ItemTwo", searchDiscovery.buildIngredientQuery(ingredients.toTypedArray()))
    }

    @Test
    fun testBuildIngredientQueryNoIngredients(){

        var ingredients = mutableListOf<Ingredient>()

        Assert.assertEquals("ingredientList:", searchDiscovery.buildIngredientQuery(ingredients.toTypedArray()))
    }

    @Test
    fun testProcessResponseEmpty(){

        var ingredients = mutableListOf<Ingredient>()
        var list = mutableListOf<QueryResult>()
        `when`(queryResponse.results).thenReturn(list)

        Assert.assertEquals(ingredients, searchDiscovery.processResponse(queryResponse, ingredients.toTypedArray()))
    }

    @Test
    fun testProcessResponseDefault(){

        var ingredients = mutableListOf<Ingredient>()
        var list = mutableListOf<QueryResult>()
        var listExpected = mutableListOf<DiscoveryRecipe>()
        var iter = queryResponse.results.iterator()

        `when`(queryResponse.results).thenReturn(list)

        ingredients.add(0, Ingredient("Item1"))
        ingredients.add(1, Ingredient("ItemTwo"))

        list.add(0, QueryResult())
        list.add(1, QueryResult())

        listExpected.add(0, JSON.nonstrict.parse(list[0].toString()))
        listExpected.add(1, JSON.nonstrict.parse(list[1].toString()))


        var tester = searchDiscovery.processResponse(queryResponse, ingredients.toTypedArray())

        Assert.assertNotEquals(0, tester.count())
        Assert.assertEquals(listExpected, tester)
    }
}