package io.github.arthurkun.generic.datastore.compose.app.domain

sealed class Animal(val name: String) {

    data object Dog : Animal("Dog")

    data object Cat : Animal("Cat")

    override fun toString(): String = name

    companion object {
        fun from(value: String): Animal {
            return when (value) {
                "Dog" -> Dog
                "Cat" -> Cat
                else -> throw Exception("Unknown animal type: $value")
            }
        }

        fun to(animal: Animal): String {
            return animal.name
        }

        val entries by lazy {
            listOf(
                Dog,
                Cat,
            )
        }
    }
}
