package com.foodbodi.utils.fitnessAPI

class FitnessAPIFactory {
    companion object {
        fun googleFit(): FitnessAPI {
           return GoogleFitnessAPI()
        }

        fun samsung() : FitnessAPI {
            return SamsungFitnessAPI()
        }

        fun getByProvider() : FitnessAPI {
            val manufacturer = android.os.Build.MANUFACTURER
            if (manufacturer.equals("samsung")) {
                return samsung()
            } else {
                return googleFit()
            }
        }
    }
}