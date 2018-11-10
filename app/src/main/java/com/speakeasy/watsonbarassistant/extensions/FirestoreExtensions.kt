package com.speakeasy.watsonbarassistant.extensions

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.speakeasy.watsonbarassistant.*

fun FirebaseFirestore.friendDocument(userId: String): DocumentReference {
    return appDocument(userId, FRIENDS_COLLECTION)
}

fun FirebaseFirestore.blockedDocument(userId: String): DocumentReference {
    return appDocument(userId, BLOCKED_COLLECTION)
}

fun FirebaseFirestore.pendingDocument(userId: String): DocumentReference {
    return appDocument(userId, PENDING_FRIENDS_COLLECTION)
}

fun FirebaseFirestore.requestDocument(userId: String): DocumentReference {
    return appDocument(userId, FRIEND_REQUEST_COLLECTION)
}

fun FirebaseFirestore.shoppingCartDocument(userId: String): DocumentReference {
    return appDocument(userId, SHOPPING_CART_COLLECTION)
}

fun FirebaseFirestore.userIngredientsDocument(userId: String): DocumentReference {
    return appDocument(userId, INGREDIENT_COLLECTION)
}

fun FirebaseFirestore.recentlyViewedDocument(userId: String): DocumentReference {
    return appDocument(userId, RECENTLY_VIEWED_COLLECTION)
}

fun FirebaseFirestore.favoritesDocument(userId: String): DocumentReference {
    return appDocument(userId, FAVORITES_COLLECTION)
}

fun FirebaseFirestore.userDocument(userId: String): DocumentReference {
    return appDocument(userId, USER_COLLECTION)
}

fun FirebaseFirestore.allUsersDocument(): DocumentReference {
    return collection(ALL_USERS_COLLECTION).document(MAIN_DOCUMENT)
}

fun FirebaseFirestore.recipeDocument(recipeId: String): DocumentReference {
    return collection(RECIPE_COLLECTION).document(recipeId)
}

fun FirebaseFirestore.appDocument(userId: String, collection: String): DocumentReference {
    return collection(APP_COLLECTION).document(userId).collection(collection).document(MAIN_DOCUMENT)
}
