package com.github.zoewithabang

import com.github.zoewithabang.model.BotMessage
import org.junit.Before

trait BotMessageSpecTrait
{
    BotMessage botMessage
    BotMessage updatedBotMessage
    BotMessage botMessage2

    @Before
    def setupBotMessageSpecTrait()
    {
        botMessage = new BotMessage("thisIsATestName", "thisIsATestMessage", "thisIsATestDescription")
        updatedBotMessage = new BotMessage("thisIsATestName", "thisIsAnUpdatedTestMessage", "thisIsAnUpdatedTestDescription")
        botMessage2 = new BotMessage("thisIsAnotherTestName", "thisIsAnotherTestMessage", "thisIsAnotherTestDescription")
    }
}
