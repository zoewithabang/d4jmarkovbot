package com.github.zoewithabang

import com.github.zoewithabang.model.Alias
import org.junit.Before

trait AliasSpecTrait
{
    Alias alias
    Alias updatedAlias
    Alias alias2

    @Before
    def setupAliasSpecTrait()
    {
        alias = new Alias("thisIsATestAlias", "thisIsATestCommand", "thisIsATestDescription")
        updatedAlias = new Alias("thisIsATestAlias", "thisIsAnUpdatedTestCommand", "thisIsAnUpdatedTestDescription")
        alias2 = new Alias("thisIsAnotherTestAlias", "thisIsAnotherTestCommand", "thisIsAnotherTestDescription")
    }
}