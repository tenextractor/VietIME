package com.tenextractor.vietime

/** Code common to both Telex and VNI */
object Common {
    /** get_tone_mark_placement() function from vi-rs/src/editing.rs
     * Get nth character to place tone mark
     *
     * # Rules:
     * 1. If a vowel contains ơ or ê, tone mark goes there
     * 2. If a vowel contains `oa`, `oe`, `oo`, `oy`, tone mark should be on the
     *    second character
     *
     * If the accent style is [`AccentStyle::Old`], then:
     * - 3. For vowel length 3 or vowel length 2 with a final consonant, put it on the second vowel character
     * - 4. Else, put it on the first vowel character
     *
     * Otherwise:
     * - 3. If a vowel has 2 characters, put the tone mark on the first one
     * - 4. Otherwise, put the tone mark on the second vowel character
     */
    fun getToneMarkPosition(outputWithoutTone: CharSequence, firstVowelIndex: Int, vowelCount: Int): Int {
        val SPECIAL_VOWEL_PAIRS = setOf("oa", "oe", "oo", "uy", "uo", "ie")

        // If there's only one vowel, then it's guaranteed that the tone mark will go there
        if (vowelCount == 1) return firstVowelIndex

            for (i in firstVowelIndex..<firstVowelIndex+vowelCount) {
                when (outputWithoutTone[i]) {
                    'ơ', 'Ơ' -> return i
                    'ê', 'Ê' -> return i
                    'â', 'Â' -> return i
                }
            }

            // Special vowels require the tone mark to be placed on the second character
            val vowel = outputWithoutTone.slice(firstVowelIndex..<firstVowelIndex+vowelCount)
            if (SPECIAL_VOWEL_PAIRS.any { vowel.contains(it, ignoreCase = true) })
                return firstVowelIndex + 1

                // If a syllable end with 2 character vowel, put it on the first character
                if (firstVowelIndex + vowelCount == outputWithoutTone.length && vowelCount == 2)
                    return firstVowelIndex

                    // Else, put tone mark on second vowel
                    return firstVowelIndex + 1
    }
}
