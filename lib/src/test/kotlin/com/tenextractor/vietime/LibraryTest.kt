package com.tenextractor.vietime

import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryTest {
    // commented out test cases are failing

    fun testList(testData: List<Pair<String, String>>) {
        for (item in testData) {
            assertEquals(item.second, Telex.telexToVietnamese(item.first),
            message = "Failed test case: <${item.first}>")
        }
    }

    fun readCsv(resourceFileName: String): List<Pair<String, String>> {
        val testData = javaClass.getResource("/${resourceFileName}")!!
            .readText()
            .lineSequence()
            .filter { it.isNotBlank() && !it.trimStart().startsWith("#") }
            .map { line ->
                val (input, expected) = line.split(',', limit = 2)
                Pair(input.trim(), expected.trim())
            }
            .toList()

        return testData
    }

    fun testCsv(resourceFileName: String) {
        testList(readCsv(resourceFileName))
    }

    @Test fun initalTest() {
        val testData = listOf(
            Pair("ddi", "đi"),
            Pair("did", "đi"),
            Pair("Did", "Đi"),
            Pair("daxd", "đã"),
            Pair("dadx", "đã"),
            Pair("dadng", "đang"),
            Pair("dandg", "đang")
        )

        testList(testData)
    }

    @Test fun monophthongTest() {
        val testData = listOf(
            Pair("ba", "ba"),
            Pair("cas", "cá"),
            Pair("khawn", "khăn"),
            Pair("mawnj", "mặn"),
            Pair("caan", "cân"),
            Pair("maatj", "mật"),
            Pair("mej", "mẹ"),
            Pair("bes", "bé"),
            Pair("bee", "bê"),
            Pair("hees", "hế"),
            Pair("bi", "bi"),
            Pair("tis", "tí"),
            Pair("kyx", "kỹ"),
            Pair("lys", "lý"),
            Pair("bof", "bò"),
            Pair("cor", "cỏ"),
            Pair("coo", "cô"),
            Pair("booj", "bộ"),
            Pair("mow", "mơ"),
            Pair("bowj", "bợ"),
            Pair("ddu", "đu"),
            Pair("ddur", "đủ"),
            Pair("duw", "dư"),
            Pair("thuws", "thứ")
        )

        testList(testData)
    }

    @Test fun diphthongTest() {
        val testData = listOf(
            Pair("vai", "vai"),
            Pair("lasi", "lái"),
            Pair("sao", "sao"),
            Pair("baor", "bảo"),
            Pair("rau", "rau"),
            Pair("baus", "báu"),
            Pair("tay", "tay"),
            Pair("mays", "máy"),
            Pair("caau", "câu"),
            Pair("gaaus", "gấu"),
            Pair("caay", "cây"),
            Pair("maay", "mây"),
            Pair("meof", "mèo"),
            Pair("keos", "kéo"),
            Pair("keeu", "kêu"),
            Pair("pheeux", "phễu"),
            Pair("mias", "mía"),
            Pair("tias", "tía"),
            Pair("bieern", "biển"),
            Pair("tieen", "tiên"),
            Pair("diuj", "dịu"),
            Pair("xius", "xíu"),
            Pair("hoa", "hoa"),
            Pair("loaf", "loà"),
            Pair("hoawts", "hoắt"),
            Pair("xoawn", "xoăn"),
            Pair("ngoawnf", "ngoằn"),
            Pair("khoer", "khoẻ"),
            Pair("loef", "loè"),
            Pair("thoir", "thỏi"),
            Pair("voif", "vòi"),
            Pair("nooif", "nồi"),
            Pair("thoois", "thối"),
            Pair("mowis", "mới"),
            Pair("howij", "hợi"),
            Pair("xooong", "xoong"),
            Pair("dduooi", "đuôi"),
            Pair("suoois", "suối"),
            Pair("muas", "múa"),
            Pair("luaj", "lụa"),
            Pair("thuee", "thuê"),
            Pair("thuees", "thuế"),
            Pair("tuaanf", "tuần"),
            Pair("thuaatj", "thuật"),
            Pair("quawn", "quăn"),
            Pair("quawngj", "quặng"),
            Pair("muwja", "mựa"),
            Pair("duwja", "dựa"),
            Pair("tui", "tui"),
            Pair("muxi", "mũi"),
            Pair("huyr", "huỷ"),
            Pair("tuys", "tuý"),
            Pair("huow", "huơ"),
            Pair("quow", "quơ"),
            Pair("thuowr", "thuở"),
            Pair("nuowcs", "nước"),
            Pair("muopws", "mướp"),
            Pair("huouw", "hươu"),
            Pair("tuwur", "tửu"),
            Pair("huwu", "hưu"),
            Pair("nguwir", "ngửi"),
            Pair("guwir", "gửi"),
            Pair("yeen", "yên"),
            Pair("yeems", "yếm"),
            Pair("yeeu", "yêu"),
            Pair("yeeus", "yếu")
        )

        testList(testData)
    }

    @Test fun triphthongTest() {
        val testData = listOf(
            Pair("nhieeuf", "nhiều"),
            Pair("kieeur", "kiểu"),
            Pair("khoai", "khoai"),
            Pair("ngoaij", "ngoại"),
            Pair("xoays", "xoáy"),
            Pair("loay", "loay"),
            Pair("ngoeo", "ngoeo"),
            Pair("ngoeof", "ngoèo"),
            Pair("ngoefo", "ngoèo"),
            Pair("khuaays", "khuấy"),
            Pair("nguaayr", "nguẩy"),
            Pair("chuoois", "chuối"),
            Pair("muooij", "muội"),
            Pair("tuowis", "tưới"),
            Pair("suwowir", "sưởi"),
            Pair("ruwowuj", "rượu"),
            Pair("buwowus", "bướu"),
            Pair("khuya", "khuya"),
            Pair("khuyeen", "khuyên"),
            Pair("chuyeenj", "chuyện"),
            Pair("khuyuj", "khuỵu"),
        )

        testList(testData)
    }

    @Test fun miscTest() {
        val testData = listOf(
            Pair("ho", "ho"),
            Pair("hosawt", "hoắt")
        )

        testList(testData)
    }

    @Test fun vowelModifierTest() {
        val testData = listOf(
            Pair("uow", "ươ"),
            Pair("uwow", "ươ"), 
            Pair("uocws", "ước"),
            Pair("duow", "dươ"),
            Pair("duowf", "dườ"), 
            Pair("thuowf", "thuờ"),
            Pair("thuawf", "thừa"),
            Pair("thuongwf", "thường"),
            Pair("quonw","quơn"),
            Pair("quown","quơn"),
            Pair("khuow","khươ"), 
            Pair("luow","lươ"), 
            Pair("cuow","cươ"), 
            Pair("truow","trươ"), 
            Pair("luaw","lưa"),
            Pair("luawr","lửa")
        )

        testList(testData)
    }

    @Test fun allSyllablesTest() {
        testCsv("all_telex_syllables.csv")
    }
}
