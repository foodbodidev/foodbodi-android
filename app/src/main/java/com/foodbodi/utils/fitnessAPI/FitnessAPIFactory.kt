package com.foodbodi.utils.fitnessAPI

class FitnessAPIFactory {
    companion object {
        fun googleFit(): FitnessAPI {
           return GoogleFitnessAPI()
        }

    }
}