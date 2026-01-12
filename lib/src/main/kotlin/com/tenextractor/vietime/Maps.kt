object Maps {
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