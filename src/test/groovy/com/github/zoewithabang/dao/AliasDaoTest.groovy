package com.github.zoewithabang.dao

import com.github.zoewithabang.AliasSpecTrait
import com.github.zoewithabang.DatabaseSpecTrait
import com.github.zoewithabang.model.Alias
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

class AliasDaoTest extends Specification implements DatabaseSpecTrait, AliasSpecTrait
{
    @Shared
    AliasDao aliasDao

    def setupSpec()
    {
        aliasDao = new AliasDao(botProperties)
    }

    def "get an alias"()
    {
        when:
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
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO aliases (alias, command, description) VALUES (?, ?, ?)",
                        [alias.getAlias(), alias.getCommand(), alias.getDescription()])
                connection.execute("INSERT INTO aliases (alias, command, description) VALUES (?, ?, ?)",
                        [alias2.getAlias(), alias2.getCommand(), alias2.getDescription()])
                retrievedRows = aliasDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 2
        retrievedRows.contains(alias)
        retrievedRows.contains(alias2)
        noExceptionThrown()
    }

    def "store an alias"()
    {
        when:
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
        (Alias)retrievedRows[0] == alias
        noExceptionThrown()
    }

    def "update an alias"()
    {
        when:
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
        (Alias)retrievedRows[0] == updatedAlias
        noExceptionThrown()
    }

    def "delete an alias"()
    {
        when:
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