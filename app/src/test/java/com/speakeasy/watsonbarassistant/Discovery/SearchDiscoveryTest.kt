package com.speakeasy.watsonbarassistant.Discovery

import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.MainMenu
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.mock

class SearchDiscoveryTest: TestCase() {

    @Test
    fun testcreateAndSetsBasicTest() {
        val mainMock = mock(MainMenu().javaClass)
        val searchDiscovery = SearchDiscovery(HandleDiscovery(mainMock))
        var ingredients = arrayOf<Ingredient>()
        ingredients[0] = Ingredient("a")

        Assert.assertEquals("(a)", searchDiscovery.createAndSets(ingredients, 0, 0))
    }
}