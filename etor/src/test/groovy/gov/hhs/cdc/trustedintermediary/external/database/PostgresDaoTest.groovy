package gov.hhs.cdc.trustedintermediary.external.database


import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.wrappers.database.ConnectionPool
import gov.hhs.cdc.trustedintermediary.wrappers.database.DatabaseCredentialsProvider
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.util.stream.Collectors
import spock.lang.Specification

class PostgresDaoTest extends Specification {
    private ConnectionPool mockConnPool
    private Connection mockConn
    private PreparedStatement mockPreparedStatement
    private ResultSet mockResultSet

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()

        mockConnPool = Mock(ConnectionPool)
        mockConn = Mock(Connection)
        mockPreparedStatement = Mock(PreparedStatement)
        mockResultSet = Mock(ResultSet)

        def mockCredentialsProvider = Mock(DatabaseCredentialsProvider)
        mockCredentialsProvider.getPassword() >> "DogCow password"

        TestApplicationContext.register(DatabaseCredentialsProvider, mockCredentialsProvider)
        TestApplicationContext.register(PostgresDao, PostgresDao.getInstance())
    }

    def "upsertData works"() {
        given:
        def tableName = "DogCow"
        def pkColumnName = "Moof"
        def columns = [
            new DbColumn(pkColumnName, "Clarus", false, Types.VARCHAR),
            new DbColumn("third_column", Timestamp.from(Instant.now()), false, Types.TIMESTAMP_WITH_TIMEZONE),
            new DbColumn("second_column_with_upsert_overwrite", Timestamp.from(Instant.now()), true, Types.TIMESTAMP_WITH_TIMEZONE),
            new DbColumn("fourth_column_null", null, false, Types.VARCHAR),
        ]
        def conflictTarget = "(" + pkColumnName + ")"

        mockConnPool.getConnection() >>  mockConn

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertData(tableName, columns, conflictTarget)

        then:
        mockConn.prepareStatement(_ as String) >> { String sqlStatement ->
            assert sqlStatement.contains(tableName)
            assert sqlStatement.count("?") == columns.size()
            assert sqlStatement.contains("ON CONFLICT (" + pkColumnName + ")")

            assert !sqlStatement.contains(", )")

            // assert that the column names in the SQL statement are in the same order as the list of DbColumns argument
            def beginningOfColumnNamesString = tableName + " ("
            def beginningOfColumnNamesIndex = sqlStatement.indexOf(beginningOfColumnNamesString) + beginningOfColumnNamesString.length()
            def endingOfColumnNamesIndex = sqlStatement.indexOf(")")
            def columnNames = sqlStatement.substring(beginningOfColumnNamesIndex, endingOfColumnNamesIndex)
            def lastFoundColumnNameIndex = -1
            for (int lcv = 0; lcv < columns.size(); lcv++) {
                def columnNameIndex = columnNames.indexOf(columns.get(lcv).name())
                assert columnNameIndex > lastFoundColumnNameIndex
                lastFoundColumnNameIndex = columnNameIndex
            }

            columns.forEach {
                if (!it.upsertOverwrite()) {
                    return
                }
                assert sqlStatement.contains(it.name() + " = EXCLUDED." + it.name())
            }

            return mockPreparedStatement
        }
        (columns.size() - 1)  * mockPreparedStatement.setObject(_ as Integer, _, _ as Integer)
        1 * mockPreparedStatement.setNull(4, Types.VARCHAR)
        1 * mockPreparedStatement.executeUpdate()
    }

    def "upsertData doesn't do any upserts if there is no upsertOverwrite and does nothing if conflict target is defined"() {
        given:
        def conflictTarget
        def tableName = "DogCow"
        def columns = [
            new DbColumn("Moof", "Clarus", false, Types.VARCHAR),
            new DbColumn("second_column_with_upsert_overwrite", Timestamp.from(Instant.now()), false, Types.TIMESTAMP_WITH_TIMEZONE),
        ]

        mockConnPool.getConnection() >>  mockConn

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        conflictTarget = null
        PostgresDao.getInstance().upsertData(tableName, columns, conflictTarget)

        then:
        mockConn.prepareStatement(_ as String) >> { String sqlStatement ->
            assert sqlStatement.contains(tableName)
            assert sqlStatement.count("?") == columns.size()
            assert !sqlStatement.contains("ON CONFLICT")

            return mockPreparedStatement
        }
        columns.size() * mockPreparedStatement.setObject(_ as Integer, _, _ as Integer)
        1 * mockPreparedStatement.executeUpdate()

        when:
        conflictTarget = "ON CONSTRAINT key"
        PostgresDao.getInstance().upsertData(tableName, columns, conflictTarget)

        then:
        mockConn.prepareStatement(_ as String) >> { String sqlStatement ->
            assert sqlStatement.contains(tableName)
            assert sqlStatement.count("?") == columns.size()
            assert sqlStatement.contains("ON CONFLICT")
            assert sqlStatement.contains(conflictTarget)
            assert sqlStatement.contains("DO NOTHING")

            return mockPreparedStatement
        }
        columns.size() * mockPreparedStatement.setObject(_ as Integer, _, _ as Integer)
        1 * mockPreparedStatement.executeUpdate()
    }

    def "upsertData unhappy path throws exception"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> { throw new SQLException() }

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        PostgresDao.getInstance().upsertData("DogCow", [
            new DbColumn("", "", false, Types.VARCHAR),
        ], null)

        then:
        thrown(SQLException)
    }

    def "fetchFirstData retrieves data"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement
        mockPreparedStatement.executeQuery() >> mockResultSet
        mockResultSet.next() >> true
        mockResultSet.getString("id") >> "1234"
        mockResultSet.getString("value") >> "DogCow"

        TestApplicationContext.register(ConnectionPool, mockConnPool)

        TestApplicationContext.injectRegisteredImplementations()

        def sqlGenerator = { connection -> connection.prepareStatement("SELECT * FROM table") }

        def converter = { resultSet ->
            return [
                id: resultSet.getString("id"),
                value: resultSet.getString("value")
            ]
        }

        when:
        def result = PostgresDao.getInstance().fetchFirstData(sqlGenerator, converter)

        then:
        result.get("id") == "1234"
        result.get("value") == "DogCow"
    }

    def "fetchFirstData fails from SQL generator"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement
        mockPreparedStatement.executeQuery() >> mockResultSet
        mockResultSet.next() >> true

        TestApplicationContext.register(ConnectionPool, mockConnPool)

        TestApplicationContext.injectRegisteredImplementations()

        def originalException = new RuntimeException("oh no!")
        def sqlGenerator = { connection -> throw originalException }

        def converter = { resultSet ->
            return [:]
        }

        when:
        PostgresDao.getInstance().fetchFirstData(sqlGenerator, converter)

        then:
        def thrownException = thrown(SQLException)
        thrownException.getCause() == originalException
    }

    def "fetchFirstData fails from converter"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement
        mockPreparedStatement.executeQuery() >> mockResultSet
        mockResultSet.next() >> true
        mockResultSet.getString("id") >> "1234"
        mockResultSet.getString("value") >> "DogCow"

        TestApplicationContext.register(ConnectionPool, mockConnPool)

        TestApplicationContext.injectRegisteredImplementations()

        def sqlGenerator = { connection -> connection.prepareStatement("SELECT * FROM table") }

        def originalException = new RuntimeException("oh no!")
        def converter = { resultSet -> throw originalException }

        when:
        PostgresDao.getInstance().fetchFirstData(sqlGenerator, converter)

        then:
        def thrownException = thrown(SQLException)
        thrownException.getCause() == originalException
    }

    def "fetchFirstData returns null when rows do not exist"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement
        mockPreparedStatement.executeQuery() >> mockResultSet
        mockResultSet.next() >> false
        mockResultSet.getString("id") >> "1234"
        mockResultSet.getString("value") >> "DogCow"

        TestApplicationContext.register(ConnectionPool, mockConnPool)

        TestApplicationContext.injectRegisteredImplementations()

        def sqlGenerator = { connection -> connection.prepareStatement("SELECT * FROM table") }

        def converter = { resultSet ->
            return [
                id: resultSet.getString("id"),
                value: resultSet.getString("value")
            ]
        }

        when:
        def result = PostgresDao.getInstance().fetchFirstData(sqlGenerator, converter)

        then:
        result == null
    }

    def "fetchManyData retrieves a set of data"() {
        given:
        def id1 = "1234"
        def id2 = "5678"
        def value1 = "DogCow"
        def value2 = "Moof!"

        def expected1 = [
            id: id1,
            value: value1,
        ]

        def expected2 = [
            id: id2,
            value: value2,
        ]

        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >>  mockPreparedStatement
        mockResultSet.next() >>> [true, true, false]
        mockResultSet.getString("id") >>> [
            id1,
            id2,
        ]
        mockResultSet.getString("value") >>> [
            value1,
            value2,
        ]
        mockPreparedStatement.executeQuery() >> mockResultSet

        TestApplicationContext.register(ConnectionPool, mockConnPool)
        TestApplicationContext.injectRegisteredImplementations()

        def sqlGenerator = { connection -> connection.prepareStatement("SELECT * FROM table") }

        def converter = { resultSet ->
            return [
                id: resultSet.getString("id"),
                value: resultSet.getString("value")
            ]
        }

        when:
        def actual = PostgresDao.getInstance().fetchManyData(sqlGenerator, converter, Collectors.toSet())

        then:
        actual instanceof Set
        actual.containsAll(Set.of(expected1, expected2))
    }

    def "fetchManyData also fails"() {
        given:
        mockConnPool.getConnection() >> mockConn
        mockConn.prepareStatement(_ as String) >> mockPreparedStatement
        mockPreparedStatement.executeQuery() >> mockResultSet
        mockResultSet.next() >>> [true, false]
        mockResultSet.getString("id") >> "1234"
        mockResultSet.getString("value") >> "DogCow"

        TestApplicationContext.register(ConnectionPool, mockConnPool)

        TestApplicationContext.injectRegisteredImplementations()

        def sqlGenerator = { connection -> connection.prepareStatement("SELECT * FROM table") }

        def originalException = new RuntimeException("oh no!")
        def converter = { resultSet -> throw originalException }

        when:
        PostgresDao.getInstance().fetchManyData(sqlGenerator, converter, Collectors.toSet())

        then:
        def thrownException = thrown(SQLException)
        thrownException.getCause() == originalException
    }
}
