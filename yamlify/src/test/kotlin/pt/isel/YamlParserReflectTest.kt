package pt.isel

import java.io.File
import org.junit.jupiter.api.assertThrows
import pt.isel.test.Books
import pt.isel.test.Classroom
import pt.isel.test.School
import pt.isel.test.Student
import java.time.LocalDate
import pt.isel.test.Professor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class YamlParserReflectTest {

    @Test fun parseStudentWithMissingProperties() {
        val yaml = """
                name: Maria Candida
                from: Oleiros"""
        assertThrows<IllegalArgumentException> {
            YamlParserReflect.yamlParser(Student::class).parseObject(yaml.reader())
        }
    }
    @Test fun parseStudent() {
        val yaml = """
                name: Maria Candida
                nr: 873435
                from: Oleiros"""
        val st = YamlParserReflect.yamlParser(Student::class).parseObject(yaml.reader())
        assertEquals("Maria Candida", st.name)
        assertEquals(873435, st.nr)
        assertEquals("Oleiros", st.from)
    }
    @Test fun parseStudentWithAddress() {
        val yaml = """
                name: Maria Candida
                nr: 873435
                from: Oleiros
                address:
                  street: Rua Rosa
                  nr: 78
                  city: Lisbon
                """
        val st = YamlParserReflect.yamlParser(Student::class).parseObject(yaml.reader())
        assertEquals("Maria Candida", st.name)
        assertEquals(873435, st.nr)
        assertEquals("Oleiros", st.from)
        assertEquals("Rua Rosa", st.address?.street)
        assertEquals(78, st.address?.nr)
        assertEquals("Lisbon", st.address?.city)
    }

    @Test fun parseSequenceOfStrings() {
        val yaml = """
            - Ola
            - Maria Carmen
            - Lisboa Capital
        """
        val seq = YamlParserReflect.yamlParser(String::class)
            .parseList(yaml.reader())
            .iterator()
        assertEquals("Ola", seq.next())
        assertEquals("Maria Carmen", seq.next())
        assertEquals("Lisboa Capital", seq.next())
        assertFalse { seq.hasNext() }
    }

    @Test fun parseSequenceOfInts() {
        val yaml = """
            - 1
            - 2
            - 3
        """
        val seq = YamlParserReflect.yamlParser(Int::class)
            .parseList(yaml.reader())
            .iterator()
        assertEquals(1, seq.next())
        assertEquals(2, seq.next())
        assertEquals(3, seq.next())
        assertFalse { seq.hasNext() }
    }
    @Test fun parseSequenceOfStudents(){
        val yaml = """
            -
              name: Maria Candida
              nr: 873435
              from: Oleiros
            - 
              name: Jose Carioca
              nr: 1214398
              from: Tamega
        """
        val seq = YamlParserReflect.yamlParser(Student::class)
            .parseList(yaml.reader())
            .iterator()
        val st1 = seq.next()
        assertEquals("Maria Candida", st1.name)
        assertEquals(873435, st1.nr)
        assertEquals("Oleiros", st1.from)
        val st2 = seq.next()
        assertEquals("Jose Carioca", st2.name)
        assertEquals(1214398, st2.nr)
        assertEquals("Tamega", st2.from)
        assertFalse { seq.hasNext() }
    }
    @Test fun parseSequenceOfStudentsWithAddresses() {
        val yaml = """
            -
              name: Maria Candida
              nr: 873435
              address:
                street: Rua Rosa
                nr: 78
                city: Lisbon
              from: Oleiros
            - 
              name: Jose Carioca
              nr: 1214398
              address:
                street: Rua Azul
                nr: 12
                city: Porto
              from: Tamega
        """
        val seq = YamlParserReflect.yamlParser(Student::class)
            .parseList(yaml.reader())
            .iterator()
        val st1 = seq.next()
        assertEquals("Maria Candida", st1.name)
        assertEquals(873435, st1.nr)
        assertEquals("Oleiros", st1.from)
        assertEquals("Rua Rosa", st1.address?.street)
        assertEquals(78, st1.address?.nr)
        assertEquals("Lisbon", st1.address?.city)
        val st2 = seq.next()
        assertEquals("Jose Carioca", st2.name)
        assertEquals(1214398, st2.nr)
        assertEquals("Tamega", st2.from)
        assertEquals("Rua Azul", st2.address?.street)
        assertEquals(12, st2.address?.nr)
        assertEquals("Porto", st2.address?.city)
        assertFalse { seq.hasNext() }
    }
    @Test fun parseSequenceOfStudentsWithAddressesAndGrades() {
        val seq = YamlParserReflect.yamlParser(Student::class)
            .parseList(yamlSequenceOfStudents.reader())
            .iterator()
        assertStudentsInSequence(seq)
    }
    @Test fun parseClassroom() {
        val yaml = """
          id: i45
          students: $yamlSequenceOfStudents
        """.trimIndent()
        val cr = YamlParserReflect.yamlParser(Classroom::class)
            .parseObject(yaml.reader())
        assertEquals("i45", cr.id)
        assertStudentsInSequence(cr.students.iterator())
    }

    @Test
    fun parseStudentWithGrades(){
        val yaml = """
                  name: Maria Candida
                  nr: 873435
                  from: Oleiros
                  address:
                    street: Rua Rosa
                    nr: 78
                    city: Lisbon
                  grades:
                    - 
                      subject: LAE
                      classification: 18
                    -
                      subject: PDM
                      classification: 15
                    -
                      subject: PC
                      classification: 19
                """.trimIndent()
        val st = YamlParserReflect.yamlParser(Student::class).parseObject(yaml.reader())
        assertEquals("Maria Candida", st.name)
        assertEquals(873435, st.nr)
        assertEquals("Oleiros", st.from)
        assertEquals("Rua Rosa", st.address?.street)
        assertEquals(78, st.address?.nr)
        assertEquals("Lisbon", st.address?.city)
        val grades = st.grades.iterator()
        val g1 = grades.next()
        assertEquals("LAE", g1.subject)
        assertEquals(18, g1.classification)
        val g2 = grades.next()
        assertEquals("PDM", g2.subject)
        assertEquals(15, g2.classification)
        val g3 = grades.next()
        assertEquals("PC", g3.subject)
        assertEquals(19, g3.classification)
        assertFalse { grades.hasNext() }
    }

    @Test
    fun parseSchool(){
        val yaml = """
            id: 2543
            name: ISEL
            location:
                street: Rua Conselheiro Emídio Navarro
                nr: 1
                city: Lisbon
            established: 1852
        """.trimIndent()
        val school = YamlParserReflect.yamlParser(School::class).parseObject(yaml.reader())
        assertEquals(2543, school.id)
        assertEquals("ISEL", school.name)
        assertEquals("Rua Conselheiro Emídio Navarro", school.address.street)
        assertEquals(1, school.address.nr)
        assertEquals("Lisbon", school.address.city)
        assertEquals(1852, school.founded)
    }

    @Test
    fun parseListOfSchools(){
        val yaml = """
            - 
                id: 2543
                name: ISEL
                location:
                    street: Rua Conselheiro Emídio Navarro
                    nr: 1
                    city: Lisbon
                established: 1852
            - 
                id: 7035
                name: IST
                location:
                    street: Avenida Rovisco Pais
                    nr: 1
                    city: Lisbon
                established: 1911
        """.trimIndent()
        val schools = YamlParserReflect.yamlParser(School::class).parseList(yaml.reader())
        assertEquals(2, schools.size)
        val s1 = schools[0]
        assertEquals(2543, s1.id)
        assertEquals("ISEL", s1.name)
        assertEquals("Rua Conselheiro Emídio Navarro", s1.address.street)
        assertEquals(1, s1.address.nr)
        assertEquals("Lisbon", s1.address.city)
        assertEquals(1852, s1.founded)
        val s2 = schools[1]
        assertEquals(7035, s2.id)
        assertEquals("IST", s2.name)
        assertEquals("Avenida Rovisco Pais", s2.address.street)
        assertEquals(1, s2.address.nr)
        assertEquals("Lisbon", s2.address.city)
        assertEquals(1911, s2.founded)
    }

    @Test
    fun ParseYamlConvert(){
        val yaml = """
            name: Dragao
            date: 2004-02-02
                """.trimIndent()
        val schools = YamlParserReflect.yamlParser(Books::class).parseObject(yaml.reader())
        assertEquals( schools.name , "Dragao" )
        assertEquals(  schools.date,LocalDate.of(2004, 2, 2) )

    }


    private fun assertStudentsInSequence(seq: Iterator<Student>) {
        val st1 = seq.next()
        assertEquals("Maria Candida", st1.name)
        assertEquals(873435, st1.nr)
        assertEquals("Oleiros", st1.from)
        assertEquals("Rua Rosa", st1.address?.street)
        assertEquals(78, st1.address?.nr)
        assertEquals("Lisbon", st1.address?.city)
        val grades1 = st1.grades.iterator()
        val g1 = grades1.next()
        assertEquals("LAE", g1.subject)
        assertEquals(18, g1.classification)
        val g2 = grades1.next()
        assertEquals("PDM", g2.subject)
        assertEquals(15, g2.classification)
        val g3 = grades1.next()
        assertEquals("PC", g3.subject)
        assertEquals(19, g3.classification)
        assertFalse { grades1.hasNext() }
        val st2 = seq.next()
        assertEquals("Jose Carioca", st2.name)
        assertEquals(1214398, st2.nr)
        assertEquals("Tamega", st2.from)
        assertEquals("Rua Azul", st2.address?.street)
        assertEquals(12, st2.address?.nr)
        assertEquals("Porto", st2.address?.city)
        val grades2 = st2.grades.iterator()
        val g4 = grades2.next()
        assertEquals("TDS", g4.subject)
        assertEquals(20, g4.classification)
        val g5 = grades2.next()
        assertEquals("LAE", g5.subject)
        assertEquals(18, g5.classification)
        assertFalse { grades2.hasNext() }
        assertFalse { seq.hasNext() }
    }

    @Test fun parseSequenceOfProfessors(){
        val yaml = """
            -
                id: 99203
                name: Professor A
             -
                id: 54632
                name: Professor B
        """.trimIndent()
        val seq = YamlParserReflect.yamlParser(Professor::class).parseSequence(yaml.reader()).iterator()
        var counter = Professor.counter
        val p1 = seq.next()
        assertEquals("Professor A", p1.name)
        assertEquals(99203, p1.id)
        assertEquals(++counter, Professor.counter)
        val p2 = seq.next()
        assertEquals("Professor B", p2.name)
        assertEquals(54632, p2.id)
        assertEquals(++counter, Professor.counter)
    }

    @Test
    fun parseSequenceThrowsForNonList(){
        val yaml = """
            id: 2543
            name: ISEL
            location:
                street: Rua Conselheiro Emídio Navarro
                nr: 1
                city: Lisbon
            established: 1852
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            YamlParserReflect.yamlParser(School::class).parseSequence(yaml.reader())
        }
    }

    @Test
    fun parseFolderEager(){
        val directoryPath = "src/test/resources"
        val fileNames = File(directoryPath).listFiles()?.map { it.name }!!
        val students = YamlParserReflect.yamlParser(Student::class).parseFolderEager(directoryPath)
        val newFileNames = File(directoryPath).listFiles()?.map { it.name }!!
        assertFalse { newFileNames.contains(fileNames[0]) }
        assertFalse { newFileNames.contains(fileNames[1]) }
        assertEquals(2, students.size)
        val st1 = students[0]
        assertEquals("Maria Candida", st1.name)
        assertEquals(873435, st1.nr)
        assertEquals("Oleiros", st1.from)
        assertEquals("Rua Rosa", st1.address?.street)
        assertEquals(78, st1.address?.nr)
        assertEquals("Lisbon", st1.address?.city)
        val grades1 = st1.grades.iterator()
        val g1 = grades1.next()
        assertEquals("LAE", g1.subject)
        assertEquals(18, g1.classification)
        val g2 = grades1.next()
        assertEquals("PDM", g2.subject)
        assertEquals(15, g2.classification)
        val g3 = grades1.next()
        assertEquals("PC", g3.subject)
        assertEquals(19, g3.classification)
        assertFalse { grades1.hasNext() }
        val st2 = students[1]
        assertEquals("Jose Carioca", st2.name)
        assertEquals(1214398, st2.nr)
        assertEquals("Tamega", st2.from)
        assertEquals("Rua Azul", st2.address?.street)
        assertEquals(12, st2.address?.nr)
        assertEquals("Porto", st2.address?.city)
        val grades2 = st2.grades.iterator()
        val g4 = grades2.next()
        assertEquals("TDS", g4.subject)
        assertEquals(20, g4.classification)
        val g5 = grades2.next()
        assertEquals("LAE", g5.subject)
        assertEquals(18, g5.classification)
        assertFalse { grades2.hasNext() }
    }

    @Test
    fun parseFolderLazy(){
        val directoryPath = "src/test/resources"
        val fileNames = File(directoryPath).listFiles()?.map { it.name }!!
        val seq = YamlParserReflect.yamlParser(Student::class).parseFolderLazy(directoryPath).iterator()
        val st1 = seq.next()
        assertFalse { File(directoryPath).listFiles()?.map { it.name }!!.contains(fileNames[0]) }
        assertTrue { File(directoryPath).listFiles()?.map { it.name }!!.contains(fileNames[1]) }
        assertEquals("Maria Candida", st1.name)
        assertEquals(873435, st1.nr)
        assertEquals("Oleiros", st1.from)
        assertEquals("Rua Rosa", st1.address?.street)
        assertEquals(78, st1.address?.nr)
        assertEquals("Lisbon", st1.address?.city)
        val grades1 = st1.grades.iterator()
        val g1 = grades1.next()
        assertEquals("LAE", g1.subject)
        assertEquals(18, g1.classification)
        val g2 = grades1.next()
        assertEquals("PDM", g2.subject)
        assertEquals(15, g2.classification)
        val g3 = grades1.next()
        assertEquals("PC", g3.subject)
        assertEquals(19, g3.classification)
        assertFalse { grades1.hasNext() }
        val st2 = seq.next()
        assertFalse { File(directoryPath).listFiles()?.map { it.name }!!.contains(fileNames[1]) }
        assertEquals("Jose Carioca", st2.name)
        assertEquals(1214398, st2.nr)
        assertEquals("Tamega", st2.from)
        assertEquals("Rua Azul", st2.address?.street)
        assertEquals(12, st2.address?.nr)
        assertEquals("Porto", st2.address?.city)
        val grades2 = st2.grades.iterator()
        val g4 = grades2.next()
        assertEquals("TDS", g4.subject)
        assertEquals(20, g4.classification)
        val g5 = grades2.next()
        assertEquals("LAE", g5.subject)
        assertEquals(18, g5.classification)
        assertFalse { grades2.hasNext() }
        assertFalse { seq.hasNext() }
    }
}

const val yamlSequenceOfStudents = """
            -
              name: Maria Candida
              nr: 873435
              address:
                street: Rua Rosa
                nr: 78
                city: Lisbon
              from: Oleiros
              grades:
                - 
                  subject: LAE
                  classification: 18
                -
                  subject: PDM
                  classification: 15
                -
                  subject: PC
                  classification: 19
            - 
              name: Jose Carioca
              nr: 1214398
              address:
                street: Rua Azul
                nr: 12
                city: Porto
              from: Tamega
              grades:
                -
                  subject: TDS
                  classification: 20
                - 
                  subject: LAE
                  classification: 18
        """

