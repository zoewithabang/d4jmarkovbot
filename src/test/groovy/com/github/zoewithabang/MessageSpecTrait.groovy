package com.github.zoewithabang

import com.github.zoewithabang.model.MessageData
import org.junit.Before

import java.time.Instant

trait MessageSpecTrait
{
    MessageData message
    MessageData updatedMessage
    MessageData message2
    MessageData message3
    MessageData messageSameUser
    MessageData messageSameUser2
    MessageData messageSameUser3

    @Before
    def setupMessageSpecTrait()
    {
        message = new MessageData("thisIsATestId", "thisIsATestUser", "thisIsATestContent", Instant.now().toEpochMilli()- 10000)
        updatedMessage = new MessageData("thisIsATestId", "thisIsTestUser2", "thisIsAnotherTestContent", Instant.now().toEpochMilli() - 1000)
        message2 = new MessageData("thisIsTestId2", "thisIsTestUser2", "thisIsTestContent2", Instant.now().toEpochMilli() - 123456789)
        message3 = new MessageData("thisIsTestId3", "thisIsTestUser3", "thisIsTestContent3", Instant.now().toEpochMilli() - 100000000)

        messageSameUser = new MessageData("thisIsATestId", "thisIsATestUser", "thisIsATestContent", Instant.now().toEpochMilli()- 10000)
        messageSameUser2 = new MessageData("thisIsATestId2", "thisIsATestUser", "thisIsATestContent2", Instant.now().toEpochMilli())
        messageSameUser3 = new MessageData("thisIsATestId3", "thisIsATestUser", "thisIsATestContent3", Instant.now().toEpochMilli()- 123456789)
    }
}