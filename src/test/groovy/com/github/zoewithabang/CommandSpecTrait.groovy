package com.github.zoewithabang

import com.github.zoewithabang.model.CommandInfo
import org.junit.Before

trait CommandSpecTrait
{
    CommandInfo command
    CommandInfo updatedCommand
    CommandInfo command2

    @Before
    def setupCommandSpecTrait()
    {
        command = new CommandInfo("thisIsACommandName", true, 0)
        updatedCommand = new CommandInfo("thisIsACommandName", false, 255)
        command2 = new CommandInfo("thisIsAnotherCommandName", false, 255)
    }
}