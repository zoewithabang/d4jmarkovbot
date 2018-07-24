package com.github.zoewithabang.dao

import com.github.zoewithabang.TestHelper
import com.github.zoewithabang.model.Alias
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

class AliasDaoTest extends Specification
{
    @Shared
    AliasDao aliasDao
    @Shared
    String dbUrl
    @Shared
    Properties dbProperties
    @Shared
    String dbDriver

    def setupSpec()
    {
        Properties botProperties = TestHelper.getBotProperties()
        dbUrl = TestHelper.getDbUrl(botProperties)
        dbProperties = TestHelper.getDbProperties(botProperties)
        dbDriver = TestHelper.getDbDriver()

        aliasDao = new AliasDao(botProperties)
    }

    def "get an alias"()
    {
        when:
        def alias = new Alias("thisIsATestAlias", "thisIsATestCommand", "thisIsATestDescription")
        def retrievedAlias
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO aliases (alias, command, description) VALUES (?, ?, ?)",
                        [alias.getAlias(), alias.getCommand(), alias.getDescription()])
                retrievedAlias = aliasDao.get(connection.getConnection(), alias.getAlias())
                transaction.rollback()
            }
        }

        then:
        retrievedAlias == alias
        noExceptionThrown()
    }

    def "get all aliases"()
    {
        when:
        def alias1 = new Alias("thisIsATestAlias", "thisIsATestCommand", "thisIsATestDescription")
        def alias2 = new Alias("thisIsAnotherTestAlias", "thisIsAnotherTestCommand", "thisIsAnotherTestDescription")
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO aliases (alias, command, description) VALUES (?, ?, ?)",
                        [alias1.getAlias(), alias1.getCommand(), alias1.getDescription()])
                connection.execute("INSERT INTO aliases (alias, command, description) VALUES (?, ?, ?)",
                        [alias2.getAlias(), alias2.getCommand(), alias2.getDescription()])
                retrievedRows = aliasDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 2
        retrievedRows.contains(alias1)
        retrievedRows.contains(alias2)
        noExceptionThrown()
    }

    def "store an alias"()
    {
        when:
        def alias = new Alias("thisIsATestAlias", "thisIsATestCommand", "thisIsATestDescription")
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                aliasDao.store(connection.getConnection(), alias)
                retrievedRows = connection.rows("SELECT * FROM aliases WHERE alias = ?",
                        [alias.getAlias()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (Alias)retrievedRows.getAt(0) == alias
        noExceptionThrown()
    }

    def "update an alias"()
    {
        when:
        def alias = new Alias("thisIsATestAlias", "thisIsATestCommand", "thisIsATestDescription")
        def updatedAlias = new Alias("thisIsATestAlias", "thisIsAnUpdatedTestCommand", "thisIsAnUpdatedTestDescription")
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO aliases (alias, command, description) VALUES (?, ?, ?)",
                        [alias.getAlias(), alias.getCommand(), alias.getDescription()])
                aliasDao.update(connection.getConnection(), updatedAlias)
                retrievedRows = connection.rows("SELECT * FROM aliases WHERE alias = ?",
                        [alias.getAlias()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (Alias)retrievedRows.getAt(0) == updatedAlias
        noExceptionThrown()
    }

    def "delete an alias"()
    {
        when:
        def alias = new Alias("thisIsATestAlias", "thisIsATestCommand", "thisIsATestDescription")
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO aliases (alias, command, description) VALUES (?, ?, ?)",
                        [alias.getAlias(), alias.getCommand(), alias.getDescription()])
                aliasDao.delete(connection.getConnection(), alias)
                retrievedRows = connection.rows("SELECT * FROM aliases WHERE alias = ?",
                        [alias.getAlias()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 0
        noExceptionThrown()
    }
}