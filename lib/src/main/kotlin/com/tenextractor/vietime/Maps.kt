package com.tenextractor.vietime

object Maps {
    val CONSONANTS = setOf(
        'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'z')
    
    val VOWELS = setOf('a', 'e', 'i', 'o', 'u', 'y', 'A', 'E', 'I', 'O', 'U', 'Y')

    val VOWELS_WITH_DIACRITICS = setOf(
        'a', 'ă', 'â', 'e', 'ê', 'i', 'o', 'ô', 'ơ', 'u', 'ư', 'y',
        'A', 'Ă', 'Â', 'E', 'Ê', 'I', 'O', 'Ô', 'Ơ', 'U', 'Ư', 'Y'
    )

    /** A map of characters without accent to character with circumflex accent */
    public val CIRCUMFLEX_MAP = mapOf(
        'a' to 'â',
        'e' to 'ê',
        'o' to 'ô',
        // uppercase
        'A' to 'Â',
        'E' to 'Ê',
        'O' to 'Ô',
    )

    /** A map of characters without accent to character with dyet (D WITH STROKE) accent */
    public val STROKE_MAP = mapOf(
        'd' to 'đ',
        'D' to 'Đ',
    )

    /** A map of characters without accent to character with horn accent */
    public val HORN_MAP = mapOf(
        'u' to 'ư',
        'o' to 'ơ',
        // uppercase
        'U' to 'Ư',
        'O' to 'Ơ',
    )

    /** A map of characters without accent to character with breve accent */
    public val BREVE_MAP = mapOf(
        'a' to 'ă',
        // uppercase
        'A' to 'Ă',
    )
}
