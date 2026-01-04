fun main() {
    // add JS here
//     const input = document.getElementById('input');
//     const output = document.getElementById('output');
//
//     input.addEventListener('input', () => {
//         output.value = processInput(input.value);
//     });
//
//     function processInput(input) {
//         let buffer = "";
//         let output = "";
//
//         for (const ch of input) {
//             if (!isAsciiLetter(ch)) {
//                 const bufferOutput = (buffer != "") ? Telex_getInstance().telexToVietnamese_gzovua_k$(buffer) : "";
//                 buffer = "";
//                 output += bufferOutput + ch;
//             } else buffer += ch;
//         }
//
//         if (buffer != "") output += Telex_getInstance().telexToVietnamese_gzovua_k$(buffer);
//         return output;
//     }
//
//     function isAsciiLetter(char) {
//         const code = char.charCodeAt(0);
//         return (code >= 65 && code <= 90) || (code >= 97 && code <= 122);
//     }
    println(Telex.telexToVietnamese("nguoiwf"))
}

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

            // if (!startedFinal) {
            //     if (startedVowel && CONSONANTS.contains(ch) && !MODIFIERS.contains(ch))
            //         startedFinal = true
            // }

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
                        } else {
                            output.append(Maps.CIRCUMFLEX_MAP[ch])
                            vowelCount++
                        }

                        continue // after outputting the character with diacritic,
                        // suppress outputting the original character
                    }

                    val wIndices = modifierIndices['w']!!
                    if (wIndices.size == 1) {
                        if (lowercaseCh == 'a') output.append(Maps.BREVE_MAP[ch])
                        if (lowercaseCh == 'o') output.append(Maps.HORN_MAP[ch])
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

                    if (modifierIndices['w']!!.size == 1) {
                        output.append(Maps.HORN_MAP[ch])
                        vowelCount++
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

object Maps {
    /** A map of characters without accent to character with circumflex accent */
    public val CIRCUMFLEX_MAP = mapOf(
        'a' to 'â',
        'e' to 'ê',
        'o' to 'ô',
        'ạ' to 'ậ',
        'ẹ' to 'ệ',
        'ọ' to 'ộ',
        'á' to 'ấ',
        'é' to 'ế',
        'ó' to 'ố',
        'ả' to 'ẩ',
        'ẻ' to 'ể',
        'ỏ' to 'ổ',
        'ã' to 'ẫ',
        'ẽ' to 'ễ',
        'õ' to 'ỗ',
        'à' to 'ầ',
        'è' to 'ề',
        'ò' to 'ồ',
        // uppercase
        'A' to 'Â',
        'E' to 'Ê',
        'O' to 'Ô',
        'Ạ' to 'Ậ',
        'Ẹ' to 'Ệ',
        'Ọ' to 'Ộ',
        'Á' to 'Ấ',
        'É' to 'Ế',
        'Ó' to 'Ố',
        'Ả' to 'Ẩ',
        'Ẻ' to 'Ể',
        'Ỏ' to 'Ổ',
        'Ã' to 'Ẫ',
        'Ẽ' to 'Ễ',
        'Õ' to 'Ỗ',
        'À' to 'Ầ',
        'È' to 'Ề',
        'Ò' to 'Ồ',
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
        'ú' to 'ứ',
        'ó' to 'ớ',
        'ù' to 'ừ',
        'ò' to 'ờ',
        'ủ' to 'ử',
        'ỏ' to 'ở',
        'ũ' to 'ữ',
        'õ' to 'ỡ',
        'ọ' to 'ợ',
        'ụ' to 'ự',
        // uppercase
        'U' to 'Ư',
        'O' to 'Ơ',
        'Ú' to 'Ứ',
        'Ó' to 'Ớ',
        'Ù' to 'Ừ',
        'Ò' to 'Ờ',
        'Ủ' to 'Ử',
        'Ỏ' to 'Ở',
        'Ũ' to 'Ữ',
        'Õ' to 'Ỡ',
        'Ọ' to 'Ợ',
        'Ụ' to 'Ự',
    )

    /** A map of characters without accent to character with breve accent */
    public val BREVE_MAP = mapOf(
        'a' to 'ă',
        'á' to 'ắ',
        'à' to 'ằ',
        'ả' to 'ẳ',
        'ã' to 'ẵ',
        'ạ' to 'ặ',
        // uppercase
        'A' to 'Ă',
        'Á' to 'Ắ',
        'À' to 'Ằ',
        'Ả' to 'Ẳ',
        'Ã' to 'Ẵ',
        'Ạ' to 'Ặ',
    )
}

/** Vietnamese tone marks.
 *
 * Represents the five tone marks used in Vietnamese writing system.
 */
enum class ToneMark(val map: Map<Char, Char>) {
    /** Dấu sắc (acute accent) - rising tone */
    ACUTE(mapOf(
        'a' to 'á',
        'â' to 'ấ',
        'ă' to 'ắ',
        'e' to 'é',
        'ê' to 'ế',
        'i' to 'í',
        'o' to 'ó',
        'ô' to 'ố',
        'ơ' to 'ớ',
        'u' to 'ú',
        'ư' to 'ứ',
        'y' to 'ý',
        // uppercase
        'A' to 'Á',
        'Â' to 'Ấ',
        'Ă' to 'Ắ',
        'E' to 'É',
        'Ê' to 'Ế',
        'I' to 'Í',
        'O' to 'Ó',
        'Ô' to 'Ố',
        'Ơ' to 'Ớ',
        'U' to 'Ú',
        'Ư' to 'Ứ',
        'Y' to 'Ý',
    )),
    /** Dấu huyền (grave accent) - falling tone */
    GRAVE(mapOf(
        'a' to 'à',
        'â' to 'ầ',
        'ă' to 'ằ',
        'e' to 'è',
        'ê' to 'ề',
        'i' to 'ì',
        'o' to 'ò',
        'ô' to 'ồ',
        'ơ' to 'ờ',
        'u' to 'ù',
        'ư' to 'ừ',
        'y' to 'ỳ',
        // uppercase
        'A' to 'À',
        'Â' to 'Ầ',
        'Ă' to 'Ằ',
        'E' to 'È',
        'Ê' to 'Ề',
        'I' to 'Ì',
        'O' to 'Ò',
        'Ô' to 'Ồ',
        'Ơ' to 'Ờ',
        'U' to 'Ù',
        'Ư' to 'Ừ',
        'Y' to 'Ỳ',
    )),
    /** Dấu hỏi (hook above) - dipping tone */
    HOOK(mapOf(
        'a' to 'ả',
        'â' to 'ẩ',
        'ă' to 'ẳ',
        'e' to 'ẻ',
        'ê' to 'ể',
        'i' to 'ỉ',
        'o' to 'ỏ',
        'ô' to 'ổ',
        'ơ' to 'ở',
        'u' to 'ủ',
        'ư' to 'ử',
        'y' to 'ỷ',
        // uppercase
        'A' to 'Ả',
        'Ă' to 'Ẳ',
        'Â' to 'Ẩ',
        'E' to 'Ẻ',
        'Ê' to 'Ể',
        'O' to 'Ỏ',
        'Ô' to 'Ổ',
        'Ơ' to 'Ở',
        'I' to 'Ỉ',
        'U' to 'Ủ',
        'Ư' to 'Ử',
        'Y' to 'Ỷ',
    )),
    /** Dấu ngã (tilde) - creaky rising tone */
    TILDE(mapOf(
        'a' to 'ã',
        'ă' to 'ẵ',
        'â' to 'ẫ',
        'e' to 'ẽ',
        'ê' to 'ễ',
        'o' to 'õ',
        'ô' to 'ỗ',
        'ơ' to 'ỡ',
        'i' to 'ĩ',
        'u' to 'ũ',
        'ư' to 'ữ',
        'y' to 'ỹ',
        // uppercase
        'A' to 'Ã',
        'Ă' to 'Ẵ',
        'Â' to 'Ẫ',
        'E' to 'Ẽ',
        'Ê' to 'Ễ',
        'O' to 'Õ',
        'Ô' to 'Ỗ',
        'Ơ' to 'Ỡ',
        'I' to 'Ĩ',
        'U' to 'Ũ',
        'Ư' to 'Ữ',
        'Y' to 'Ỹ',
    )),
    /** Dấu nặng (dot below) - creaky falling tone */
    DOT(mapOf(
        'a' to 'ạ',
        'ă' to 'ặ',
        'â' to 'ậ',
        'e' to 'ẹ',
        'ê' to 'ệ',
        'o' to 'ọ',
        'ô' to 'ộ',
        'ơ' to 'ợ',
        'i' to 'ị',
        'u' to 'ụ',
        'ư' to 'ự',
        'y' to 'ỵ',
        // uppercase
        'A' to 'Ạ',
        'Ă' to 'Ặ',
        'Â' to 'Ậ',
        'E' to 'Ẹ',
        'Ê' to 'Ệ',
        'O' to 'Ọ',
        'Ô' to 'Ộ',
        'Ơ' to 'Ợ',
        'I' to 'Ị',
        'U' to 'Ụ',
        'Ư' to 'Ự',
        'Y' to 'Ỵ',
    )),
}
