package com.github.zoewithabang

import com.github.zoewithabang.model.Option
import org.junit.Before

trait OptionSpecTrait
{
    Option option
    Option updatedOption
    Option option2

    @Before
    def setupOptionSpecTrait()
    {
        option = new Option("thisIsATestKey", "thisIsATestValue")
        updatedOption = new Option("thisIsATestKey", "thisIsAnotherTestValue")
        option2 = new Option("thisIsTestKey2", "thisIsTestValue2")
    }
}