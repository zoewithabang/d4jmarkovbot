package com.github.zoewithabang

import com.github.zoewithabang.model.UserData
import org.junit.Before

trait UserSpecTrait
{
    UserData user
    UserData updatedUser
    UserData user2
    UserData user3

    @Before
    def setupUserSpecTrait()
    {
        user = new UserData("thisIsATestUser", true, 0)
        updatedUser = new UserData("thisIsATestUser", false, 255)
        user2 = new UserData("thisIsTestUser2", false, 255)
        user3 = new UserData("thisIsTestUser3", true, 100)
    }
}