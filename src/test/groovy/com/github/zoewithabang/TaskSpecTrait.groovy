package com.github.zoewithabang

import com.github.zoewithabang.model.TaskInfo
import org.junit.Before

trait TaskSpecTrait
{
    TaskInfo task
    TaskInfo updatedTask
    TaskInfo task2

    @Before
    def setupTaskSpecTrait()
    {
        task = new TaskInfo("thisIsATaskName", true, 0, 1)
        updatedTask = new TaskInfo("thisIsATaskName", false, 10, 10)
        task2 = new TaskInfo("thisIsAnotherTaskName", false, 10, 10)
    }
}