package gov.hhs.cdc.trustedintermediary.external.database

import java.sql.ResultSet
import java.sql.SQLException
import spock.lang.Specification

class ResultSetIteratorTest extends Specification {
    def "hasNext returns true until empty due to naturally running out of items"() {
        given:
        def mockResults = Mock(ResultSet)
        mockResults.next() >>> [true, true, false]

        def iterator = new ResultSetIterator(mockResults)

        when:
        def first = iterator.hasNext()
        def second = iterator.hasNext()
        def third = iterator.hasNext()
        def fourth = iterator.hasNext()

        then:
        first
        second
        !third
        !fourth
    }

    def "hasNext returns true until empty due to throwing an exception"() {
        given:
        def mockResults = Mock(ResultSet)
        mockResults.next() >>> [true, true] >> { throw new SQLException("SQL exception occurred.") }
        def iterator = new ResultSetIterator(mockResults)

        when:
        def first = iterator.hasNext()
        def second = iterator.hasNext()
        iterator.hasNext()

        then:
        first
        second
        thrown(RuntimeException)
    }

    def "next returns the result set until empty"() {
        given:
        def mockResults = Mock(ResultSet)
        mockResults.next() >>> [true, true, false]
        def iterator = new ResultSetIterator(mockResults)

        when:
        iterator.hasNext()
        def first = iterator.next()
        iterator.hasNext()
        def second = iterator.next()
        iterator.hasNext()
        iterator.next()

        then:
        first == mockResults
        second == mockResults
        thrown(NoSuchElementException)
    }
}
