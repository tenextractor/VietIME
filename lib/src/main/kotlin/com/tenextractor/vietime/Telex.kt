object Telex {
    val CONSONANTS = setOf(
        'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'z')
    val VOWELS = setOf('a', 'e', 'i', 'o', 'u', 'y', 'A', 'E', 'I', 'O', 'U', 'Y')

    val INITIALS = setOf("ch", "gh", "kh", "nh", "ng", "ph", "th", "tr", "dd", "ngh")

    /** Set of letters that are used to add diacritics (breve, cirumflex, etc.)
     * or tones (acute, grave, etc.)
     */
    val MODIFIERS = setOf(
        'a', 'd', 'e', 'f', 'j', 'o', 'r', 's', 'w', 'x')
    val DIACRITICS = setOf('a', 'd', 'e', 'o', 'w')
    val TONES = mapOf(
        'f' to ToneMark.GRAVE,
        'j' to ToneMark.DOT,
        'r' to ToneMark.HOOK,
        's' to ToneMark.ACUTE,
        'x' to ToneMark.TILDE
    )

    /** These are the modifiers that should only be active if they come after the first vowel letter.
     * For example, `sao` should not output any tone marks, but `aso` should output `áo`.
     */
    val AFTER_VOWEL_MODIFIERS = setOf('f', 'j', 'r', 's', 'w', 'x')

    // TODO: "lookahead" function that ignores tones to fix the "ngoeo" issue
    fun matchWithoutTones(lowercaseInput: String, startIndex: Int, match: String): Boolean {
        // If the rest of lowercaseInput is shorter than the string to be matched, just return false
        if (lowercaseInput.length - startIndex < match.length) return false

        var inputIndex = startIndex
        for (ch in match) {
            while (inputIndex < lowercaseInput.length && TONES.containsKey(lowercaseInput[inputIndex]))
                inputIndex++
            if (inputIndex == lowercaseInput.length) return false
            if (ch != lowercaseInput[inputIndex]) return false
            inputIndex++
        }
        return true
    }

    /** Convert a string that represents a Vietnamese syllable written in the Telex convention ([input])
     * to a syllable written in Vietnamese orthography.
     * Example: input = "vietej", output = "việt"
    */
    public fun telexToVietnamese(input: String): String {

        // STAGE 1: calculate modifierIndices and firstVowelIndex
        // Example:
        //   Input: "vietej"
        //   Output:
        //     modifierIndices: { 'e': [2, 4], 'j': [5], the rest are empty lists }
        //     firstVowelIndex: 1
        val lowercaseInput = input.lowercase()
        var startedVowel = false
        var startedFinal = false
        var firstVowelIndex = -1

        val lowercaseVowel = StringBuilder()

        /** Map of 'modifier' characters that can add a diacritic or tone mark,
         * to lists of indices of occurrences of these characters
         * (also includes 'u', which is not a modifier, but is rather modified by 'w' to become 'ư')
         */
        val modifierIndices: Map<Char, MutableList<Int>> = mapOf(
            'a' to mutableListOf(),
            'd' to mutableListOf(),
            'e' to mutableListOf(),
            'f' to mutableListOf(),
            'j' to mutableListOf(),
            'o' to mutableListOf(),
            'r' to mutableListOf(),
            's' to mutableListOf(),
            'u' to mutableListOf(),
            'w' to mutableListOf(),
            'x' to mutableListOf(),
        )

        for ((index, ch) in lowercaseInput.withIndex()) {

            if (!startedVowel) {
                if (VOWELS.contains(ch)) {
                    // TODO: this code needs to be refined further
                    // if a syllable has a weird initial (like 'cl' in 'clown') that we are sure does not belong to Vietnamese,
                    // then stop the conversion process and just output the input as it is
                    // if (!(index in 0..3)) return input
                    // if (index in 2..3)
                    //     if (!INITIALS.contains(lowercaseInput.slice(0..<index)))
                    //         return input

                    firstVowelIndex = index
                    startedVowel = true
                }
            }

            if (startedVowel && !startedFinal) {
                if (!AFTER_VOWEL_MODIFIERS.contains(ch))
                    lowercaseVowel.append(ch)

                if (startedVowel && CONSONANTS.contains(ch) && !MODIFIERS.contains(ch))
                    startedFinal = true
            }

            if (AFTER_VOWEL_MODIFIERS.contains(ch)) {
                if (startedVowel) modifierIndices[ch]!!.add(index)
            } else if (modifierIndices.containsKey(ch)) {
                modifierIndices[ch]!!.add(index)
            }
        }


        // STAGE 1.5: apply a correction to firstVowelIndex
        // If the input contains more than one 'd' before the vowel starts
        // (example: "ddi" > "đi", "dddi" > "ddi"), one of the characters will be deleted
        // and therefore the firstVowelIndex needs to be corrected to account for this
        if (modifierIndices['d']!!.size > 1 && modifierIndices['d']!!.last() < firstVowelIndex)
            firstVowelIndex--
        
        // apply correction to lowercaseVowel
        if (lowercaseVowel.length > 1 && (lowercaseInput.slice(0..<2) == "gi" || lowercaseInput.slice(0..<2) == "qu"))
            lowercaseVowel.deleteAt(0)


        // STAGE 2: use modifierIndices to apply diacritics (except tone marks) to the syllable
        // Example:
        //   Input: "vietej" with its modifierIndices and firstVowelIndex as detailed in Stage 1
        //   Output:
        //     outputWithoutTone: "viêt"
        //     tone: ToneMark.DOT
        val output = StringBuilder()
        var tone: ToneMark? = null
        var doNotOutputNextChar = false
        var vowelCount = 0
        var wHasBeenUsed = false

        for ((index, ch) in input.withIndex()) {
            if (doNotOutputNextChar) {
                doNotOutputNextChar = false
                continue
            }

            val lowercaseCh = lowercaseInput[index]

            when (lowercaseCh) {
                'a', 'd', 'e', 'o' -> {
                    // handle letters that can be doubled

                    val thisModifierIndices = modifierIndices[lowercaseCh]!!

                    // if there is a string such as `ddi` (output: `đi`) or `dddi` (output: ddi),
                    // the last `d` (or any modifier that can be doubled) needs to be omitted from the output
                    if (thisModifierIndices.size >= 2 && index == thisModifierIndices.last()) continue

                    // if there is a string such as `ddi` (output: `đi`),
                    // a diacritic needs to be applied to the first `d`
                    if (thisModifierIndices.size == 2 && index == thisModifierIndices[0]) {
                        if (lowercaseCh == 'd') {
                            output.append(Maps.STROKE_MAP[ch])
                        } else if (lowercaseCh == 'o' && matchWithoutTones(lowercaseInput, index, "oeo")) {
                            // handle "oeo" edge case (should output "oeo", not "ôe"):
                            // remove the second 'o''s index from modifierIndices so that it will be outputted
                            modifierIndices['o']!!.removeLast()
                            output.append(ch)
                        } else {
                            output.append(Maps.CIRCUMFLEX_MAP[ch])
                            vowelCount++
                        }

                        continue // after outputting the character with diacritic,
                        // suppress outputting the original character
                    }

                    val wIndices = modifierIndices['w']!!

                    if (wIndices.size == 1 && lowercaseCh == 'a' && !wHasBeenUsed) {
                        output.append(Maps.BREVE_MAP[ch])
                        wHasBeenUsed = true
                        vowelCount++
                        continue
                    }

                    if (wIndices.size == 1 && lowercaseCh == 'o'
                    && !matchWithoutTones(lowercaseInput, index, "oa")
                    // add edge case for "oaw" (should output "oă", not "ơă" or "ơa")
                    && !(firstVowelIndex != 0 && lowercaseVowel.contentEquals("ou"))
                    ) {
                        output.append(Maps.HORN_MAP[ch])
                        wHasBeenUsed = true
                        vowelCount++
                        continue
                    }
                }

                // handling tones
                'f', 'j', 'r', 's', 'x' -> {
                    val thisModifierIndices = modifierIndices[lowercaseCh]!!

                    if (thisModifierIndices.size == 1)
                        tone = TONES[lowercaseCh]!!

                    if (thisModifierIndices.size >= 1 && index == thisModifierIndices.last()) continue
                }

                'u' -> {
                    // edge case for `uwow` > `ươ`:
                    // the first instance of
                    if (lowercaseInput.length >= index + 4) {
                        if (lowercaseInput.slice(index..<index+4) == "uwow" && modifierIndices['w']!!.size == 2) {
                            modifierIndices['w']!!.removeAt(0)
                            doNotOutputNextChar = true
                        }
                    }

                    // Check if this is a simple "uo" pattern (only one 'w' modifier and no others)
                    // This only applies for certain initials (h, th, q)
                    // For example: "huow" -> "huơ" (isSimpleUow=true), but "uow" -> "ươ" (isSimpleUow=false)
                    var isSimpleUow = false
                    val initial = lowercaseInput.substring(0, firstVowelIndex)
                    if ((initial == "h" || initial == "th" || initial == "q") && modifierIndices['w']!!.size == 1 && index + 1 < lowercaseInput.length && lowercaseInput[index+1] == 'o') {
                         isSimpleUow = true
                         // Scan for other modifiers; if found, it's not simple
                         for (k in firstVowelIndex until lowercaseInput.length) {
                             if (k == index || k == index + 1) continue

                             val c = lowercaseInput[k]
                             if (MODIFIERS.contains(c) && modifierIndices.containsKey(c) && modifierIndices[c]!!.contains(k)) {
                                 continue
                             }
                             isSimpleUow = false
                             break
                         }
                    }

                    if (modifierIndices['w']!!.size == 1 && !wHasBeenUsed && !(lowercaseInput[0] == 'q' && index == 1) && !isSimpleUow) {
                        output.append(Maps.HORN_MAP[ch])
                        vowelCount++
                        wHasBeenUsed = true
                        continue
                    }
                }

                'w' -> {
                    if (modifierIndices['w']!!.size >= 1
                        && index == modifierIndices['w']!!.last()) continue
                }
            }

            output.append(ch) // default behavior: just output the character from input as it is
            if (VOWELS.contains(lowercaseCh)) vowelCount++
        }

        // STAGE 3: apply a tone mark (if any)
        if (tone == null) return output.toString()

        // edge case: "gija" should output "gịa"
        if (lowercaseInput == "gija") {
            output[1] = tone.map[output[1]] ?: output[1]
            return output.toString()
        }

        // apply corrections to vowelCount and firstVowelIndex:
        // 'gi' (if there is another vowel after it) and 'qu' should be considered as consonants
        // There is no Vietnamese word which consists of the initial 'qu' without another vowel letter,
        // but for the sake of better error/edge case handling the correction will only be applied
        // if there is another vowel letter.
        if (vowelCount > 1 && (lowercaseInput.slice(0..<2) == "gi" || lowercaseInput.slice(0..<2) == "qu")) {
                vowelCount--
                firstVowelIndex++
        }

        // if there has been some error applying the correction, just output without the tone mark
        if (vowelCount <= 0 || firstVowelIndex < 0 || firstVowelIndex + vowelCount - 1 >= output.length)
            return output.toString()

        // add tone mark
        val toneMarkPosition = getToneMarkPosition(output, firstVowelIndex, vowelCount)
        output[toneMarkPosition] = tone.map[output[toneMarkPosition]] ?:
            output[toneMarkPosition]

        return output.toString()
    }

    /** get_tone_mark_placement() function from vi-rs/src/editing.rs
    /// Get nth character to place tone mark
    ///
    /// # Rules:
    /// 1. If a vowel contains ơ or ê, tone mark goes there
    /// 2. If a vowel contains `oa`, `oe`, `oo`, `oy`, tone mark should be on the
    ///    second character
    ///
    /// If the accent style is [`AccentStyle::Old`], then:
    /// - 3. For vowel length 3 or vowel length 2 with a final consonant, put it on the second vowel character
    /// - 4. Else, put it on the first vowel character
    ///
    /// Otherwise:
    /// - 3. If a vowel has 2 characters, put the tone mark on the first one
    /// - 4. Otherwise, put the tone mark on the second vowel character
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