package com.speakeasy.watsonbarassistant

// All Recipes Constants
const val DEFAULT_IMAGE_BASE64 = "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m25fqRO6hPXqq69iw4YN2L9/P8LhMI444gj8+Mc/Tn47vvPOO3jttdcwfvx43HvvvXC73di0aRNuuukmrFixAjfffHPa/Orr6/HAAw/gu9/9Ljo6OjB//nwceeSRg5Z7+OGH40c/+hGqqqrwu9/9Ds888wxaWlpw/vnnAxgoJUWjUdx4442YNWsWVFXFbbfdhnXr1mHHjh3JEAMGvoEfeOABXHfdddi5cyd+8IMfYMqUKZbb3tzcjJ///Oc47rjjAAxUibdu3Qqfz4fbb78dHo8HoVAIqjrwV/R2tgsArrjiCowbNw6LFy/GlClTcOuttyafa2lpwT/+8Q9UV1djyZIlqKmpwe7du3HNNdfgueeew+zZszFhwgTTdT755JNxww03wOPxYPXq1Vi0aBFWrlyJSy65BNXV1bj//vsRi8XwjW98A5deeikAYNGiRVi9ejUefvhhzJ8/37HtBICvfOUrmDRpEmbOnAlgoFR89dVXY+3atZg3b17GkojVtmisjhU72+zUPtGCSuQqodAlrJqaGhxzzDGYPn066uvr8dFHH2HRokXYsWMHgIMN2SeeeCJ2796NnTt3wu/3w+fzJduMcvGFL3wh2ag/bdo0AAONosBAcGzZsgVVVVU47LDDsHPnTnzyySfJkBrKclN9/etfT4YVMFAlbmxsRCQSwcKFC/Gvf/0L5eXlplWbXLzzzjsABra5pqYGADB27FgcddRRUFUV7777bsbXz5gxI9nwO2vWLHi9XsTjcezcuRPBYDBZ6p09e3byNaeeemrasp3cTp/Ph5kzZ2L//v1Yu3YtXn311eT6tbe357wtqTIdK3a3OZNs9okWVCxhFcm0adOSje7AQLVr6dKlePzxx7FgwQLs2rULALBixQqsWLEi7bXawTZU5eXlAJCsduzevRsAcODAAcybN2/Q9H19fY4sV984DwC33norfvvb36KlpQUtLS2YMmUK5s6di4kTJzqyTO39PProo9Me16ra2rbbIcsympqasHPnTvT09CTb8CorK9O6pRxzzDEAgFAohP379+OQQw5xbDuDwSB+/etf44033oAsy5gwYQJkWUYsFsuq865+W8zoj5VsttlMNvtECyqR27CEDiy9WbNmYenSpWhpaYGiKBg1ahQA4IILLsBZZ52VNq0sy3lZh7q6OgADJYF77rkHbnf6W5xaXXDa2LFj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"

// Discovery constants
const val ENV_ID_MIKE_DIS = "04760902-0426-4f36-857e-37e9d7e09f5e"
const val COL_ID_MIKE_DIS = "aecd1f2c-6cab-4fa4-96cf-81d8c55bf181"
const val PASSWORD_MIKE_DIS = "BERRVZvxKgto"
const val USERNAME_MIKE_DIS = "539fdfc9-4579-4861-a1d2-74660add2ba6"
const val URL_MIKE_DIS = "https://gateway.watsonplatform.net/discovery/api"
const val VERSION_DIS = "2018-08-01"
