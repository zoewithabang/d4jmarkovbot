package com.github.zoewithabang.dao

import com.github.zoewithabang.DatabaseSpecTrait
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection
import java.sql.SQLException

class DaoTest extends Specification implements DatabaseSpecTrait
{
    @Shared
    Dao dao

    def setupSpec()
    {
        dao = new Dao(botProperties) {
            Object get(Connection connection, Object id) throws SQLException { return null }

            List getAll(Connection connection) throws SQLException { return null }

            void store(Connection connection, Object object) throws SQLException {}

            void update(Connection connection, Object object) throws SQLException {}

            void delete(Connection connection, Object object) throws SQLException {}
        }
    }

    /*def "connect to database"()
    {
        when:
        dao.getConnection().withCloseable {}
        then:
        notThrown(Exception)
    }*/
}