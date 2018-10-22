package com.speakeasy.watsonbarassistant.Discovery

import com.ibm.watson.developer_cloud.discovery.v1.model.QueryResponse
import com.speakeasy.watsonbarassistant.Ingredient
import com.speakeasy.watsonbarassistant.MainMenu
import junit.framework.TestCase
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
        var ingredients = arrayOf<Ingredient>()
        ingredients[0] = Ingredient("a")

        Assert.assertEquals("(a)", searchDiscovery.createAndSets(ingredients, 0, 0))
    }
}