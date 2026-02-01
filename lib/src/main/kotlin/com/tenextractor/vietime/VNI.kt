package com.tenextractor.vietime

object VNI {
    val TONES = mapOf(
        '1' to ToneMark.ACUTE,
        '2' to ToneMark.GRAVE,
        '3' to ToneMark.HOOK,
        '4' to ToneMark.TILDE,
        '5' to ToneMark.DOT
    )

    fun VNIToVietnamese(input: String): String {
        val lowercaseInput = input.lowercase()

        val firstModifierIndex = MutableList(10) { -1 }

        val lowercaseInitial = StringBuilder()
        val lowercaseVowel = StringBuilder()

        /** This will be set if the input contains 'a' (either case) */
        var inputHasA = false
        var inputHasD = false
        /** This will be set if the input contains 'o' or 'u' */
        var hornApplicable = false
        /** This will be set if the input contains 'a', 'e' or 'o' */
        var circumflexApplicable = false

        var hasLetters = false

        var startedVowel = false
        var startedFinal = false

        var tone: ToneMark? = null

        // STAGE 1: build modifierIndices and lowercaseVowel
        for ((index, ch) in lowercaseInput.withIndex()) {
            //if (ch.isAsciiDigit()) modifierIndices[ch.digitToInt()].add(index)
            if (ch.isLetter()) hasLetters = true

                // update firstModifierIndex
                if (ch.isDigit() && firstModifierIndex[ch.digitToInt()] == -1)
                    firstModifierIndex[ch.digitToInt()] = index

                    if (!startedVowel && Maps.CONSONANTS.contains(ch)) lowercaseInitial.append(ch)

                        if (!startedFinal && Maps.VOWELS.contains(ch)) {
                            if (!startedVowel) startedVowel = true
                                lowercaseVowel.append(ch)
                        }

                        if (startedVowel && Maps.CONSONANTS.contains(ch))
                            startedFinal = true

                            when (ch) {
                                'a' -> {
                                    inputHasA = true
                                    circumflexApplicable = true
                                }
                                'd' -> inputHasD = true
                                'e' -> circumflexApplicable = true
                                'o' -> {
                                    circumflexApplicable = true
                                    hornApplicable = true
                                }
                                'u' -> hornApplicable = true
                                '1', '2', '3', '4', '5' -> tone = TONES[ch]!!
                            }
        }

        // apply correction to lowercaseInitial and lowercaseVowel
        var giQuCorrectionApplied = false
        if (lowercaseVowel.length > 1 && (lowercaseInitial.contentEquals("q") && lowercaseVowel[0] == 'u' ||
            lowercaseInitial.contentEquals("g") && lowercaseVowel[0] == 'i'
        )) {
            giQuCorrectionApplied = true
            lowercaseInitial.append(lowercaseVowel[0])
            lowercaseVowel.deleteAt(0)
        }

        if (!hasLetters) return input

            // STAGE 2: remove numbers and add diacritics
            val output = StringBuilder()

            /** Tracks if an 'u' has been converted to 'ư'.
             * This variable is checked to ensure that only the first 'u' is converted to 'ư' when there are multiple 'u's.
             * For example, "uou7" should output "ươu", not "ươư"; "uu7" should output "ưu", not "ưư".*/
            var uHornOutputted = false
            for ((index, ch) in lowercaseInput.withIndex()) {
                when (ch) {
                    // handle numbers
                    '1', '2', '3', '4', '5' -> if (lowercaseVowel.length > 0 && TONES[ch]!! == tone && firstModifierIndex[ch.digitToInt()] == index) continue
                    '6' -> if (circumflexApplicable && firstModifierIndex[6] == index) continue
                    '8' -> if (inputHasA && firstModifierIndex[8] == index) continue
                    // manually suppressing '7's based on how many horns were applied is too complicated and is not an essential feature anyway,
                    // so if an 'o' or 'u' is detected in the input, all '7's are removed from the output
                    '7' -> if (hornApplicable) continue
                    '9' -> if (inputHasD && firstModifierIndex[9] == index) continue

                    // handle modifiable characters
                    'a' -> {
                        if (firstModifierIndex[8] != -1) {
                            output.append(Maps.BREVE_MAP[input[index]])
                            continue
                        }

                        if (firstModifierIndex[6] != -1) {
                            output.append(Maps.CIRCUMFLEX_MAP[input[index]])
                            continue
                        }
                    }
                    'd' -> if (firstModifierIndex[9] != -1) {
                        output.append(Maps.STROKE_MAP[input[index]])
                        continue
                    }
                    'e', 'o' -> {
                        if (firstModifierIndex[6] != -1) {
                            output.append(Maps.CIRCUMFLEX_MAP[input[index]])
                            continue
                        }

                        if (ch == 'o' && firstModifierIndex[7] != -1 &&
                            !(output.length != 0 && lowercaseVowel.contentEquals("ou") && !startedFinal)) {
                            output.append(Maps.HORN_MAP[input[index]])
                            continue
                            }
                    }

                    'u' -> if (firstModifierIndex[7] != -1 &&
                    !uHornOutputted &&
                    !(output.getOrNull(0)?.lowercaseChar() == 'q' && output.length == 1) &&
                    !(output.length != 0 && lowercaseVowel.contentEquals("uo") && !startedFinal)) {
                        output.append(Maps.HORN_MAP[input[index]])
                        uHornOutputted = true
                        continue
                    }
                }

                //default behavior: output the char in input
                output.append(input[index])
            }

            // STAGE 3: add tone mark
            if (tone == null) return output.toString()

                //edge case for gi5a > gịa
                if (lowercaseInput == "gi5a") {
                    output[1] = tone.map[output[1]] ?: output[1]
                    return output.toString()
                }

                //calculate firstVowelIndex
                var firstVowelIndex = -1
                var vowelCount = lowercaseVowel.length
                for ((index, ch) in output.withIndex()) {
                    if (Maps.VOWELS_WITH_DIACRITICS.contains(ch)) {
                        firstVowelIndex = index
                        break
                    }
                }
                if (giQuCorrectionApplied) firstVowelIndex++

                val toneMarkPosition = Common.getToneMarkPosition(output, firstVowelIndex, vowelCount)
                output[toneMarkPosition] = tone.map[output[toneMarkPosition]] ?: output[toneMarkPosition]

                return output.toString()
    }
}
