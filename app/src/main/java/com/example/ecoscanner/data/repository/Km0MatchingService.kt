package com.example.ecoscanner.data.repository

import com.example.ecoscanner.data.network.OpenFoodFactsClient
import com.example.ecoscanner.data.network.ProductDto

data class Km0Suggestion(
    val product: ProductDto,
    val matchScore: Int,            // 0-100
    val matchReason: String          // texto explicativo
)

class Km0MatchingService {

    companion object {
        // Score mínimo para considerar que vale la pena recomendar
        const val MIN_SCORE_THRESHOLD = 30
        const val MAX_SUGGESTIONS = 3

        // Pesos del algoritmo de matching
        private const val WEIGHT_EXACT_CATEGORY = 50
        private const val WEIGHT_SUBCATEGORY = 25
        private const val WEIGHT_PARENT_CATEGORY = 15
        private const val WEIGHT_LABEL_MATCH = 10
        private const val WEIGHT_NUTRISCORE_MATCH = 5
        private const val WEIGHT_ECOSCORE_MATCH = 5
    }

    // Devuelve hasta 3 alternativas locales españolas para el producto dado.
    // Si no encuentra nada con score suficiente, devuelve lista vacía.
    suspend fun findLocalAlternatives(scannedProduct: ProductDto): List<Km0Suggestion> {
        // Si el producto no tiene categorías, no podemos hacer matching
        val categories = scannedProduct.categoriesTags
            ?.filter { it.isNotBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?: return emptyList()

        // Estrategia: probamos primero la categoría más específica
        // (la última en la lista de categories_tags suele ser la más específica)
        // Si no encontramos nada, probamos la siguiente más genérica
        val categoriesToTry = categories.reversed().take(3)

        for (category in categoriesToTry) {
            // El tag viene como "en:chocolate-cookies", la API quiere solo "chocolate-cookies"
            val cleanCategory = category.removePrefix("en:")
                .removePrefix("es:")
                .removePrefix("fr:")

            val results = try {
                OpenFoodFactsClient.api.searchByCategoryAndCountry(
                    category = cleanCategory,
                    country = "spain"
                ).products ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            if (results.isEmpty()) continue

            // Filtramos: que no sea el mismo producto, que tenga nombre, que sea de España
            val filtered = results.filter { candidate ->
                candidate.code != scannedProduct.code &&
                        !candidate.productName.isNullOrBlank() &&
                        isLikelySpanish(candidate)
            }

            // Calculamos score y ordenamos
            val scored = filtered
                .map { candidate -> scoreMatch(scannedProduct, candidate) }
                .filter { it.matchScore >= MIN_SCORE_THRESHOLD }
                .sortedByDescending { it.matchScore }
                .distinctBy { it.product.productName?.lowercase() } // dedupe por nombre

            if (scored.isNotEmpty()) {
                return scored.take(MAX_SUGGESTIONS)
            }
        }

        return emptyList()
    }

    // Score de coincidencia entre el producto escaneado y un candidato
    private fun scoreMatch(scanned: ProductDto, candidate: ProductDto): Km0Suggestion {
        var score = 0
        val reasons = mutableListOf<String>()

        val scannedCats = scanned.categoriesTags.orEmpty().toSet()
        val candidateCats = candidate.categoriesTags.orEmpty().toSet()

        // Exacta = comparten la categoría más específica de scanned
        val mostSpecific = scanned.categoriesTags?.lastOrNull()
        if (mostSpecific != null && candidateCats.contains(mostSpecific)) {
            score += WEIGHT_EXACT_CATEGORY
            reasons += "mateixa categoria"
        }

        // Sub/parent compartidas (intersección)
        val sharedCats = scannedCats.intersect(candidateCats)
        val sharedNonExact = sharedCats.filter { it != mostSpecific }
        if (sharedNonExact.size >= 2) {
            score += WEIGHT_SUBCATEGORY
        } else if (sharedNonExact.size == 1) {
            score += WEIGHT_PARENT_CATEGORY
        }

        // Labels compartidas (bio, fair-trade, etc.)
        val sharedLabels = scanned.labelsTags.orEmpty().toSet()
            .intersect(candidate.labelsTags.orEmpty().toSet())
        if (sharedLabels.isNotEmpty()) {
            score += WEIGHT_LABEL_MATCH
            reasons += "etiquetes similars"
        }

        // Mismo nutriscore (calidad nutricional similar)
        if (!scanned.nutriscoreGrade.isNullOrBlank() &&
            scanned.nutriscoreGrade == candidate.nutriscoreGrade) {
            score += WEIGHT_NUTRISCORE_MATCH
        }

        // Mismo ecoscore o mejor en candidato
        if (!candidate.ecoscoreGrade.isNullOrBlank() &&
            candidate.ecoscoreGrade <= (scanned.ecoscoreGrade ?: "z")) {
            score += WEIGHT_ECOSCORE_MATCH
        }

        // Cap a 100
        val finalScore = score.coerceAtMost(100)

        val reasonText = when {
            finalScore >= 85 -> "Coincidència excel·lent"
            finalScore >= 60 -> "Bona coincidència"
            finalScore >= 30 -> "Coincidència parcial"
            else -> "Coincidència feble"
        }

        return Km0Suggestion(
            product = candidate,
            matchScore = finalScore,
            matchReason = reasonText
        )
    }

    // Heurística: el producto candidato es realmente español?
    private fun isLikelySpanish(product: ProductDto): Boolean {
        val countries = listOfNotNull(
            product.origins,
            product.countries,
            product.countriesTags?.joinToString(",")
        ).joinToString(",").lowercase()

        return countries.contains("spain") ||
                countries.contains("españa") ||
                countries.contains("espanya") ||
                countries.contains("en:spain") ||
                countries.contains("es:espana")
    }
}