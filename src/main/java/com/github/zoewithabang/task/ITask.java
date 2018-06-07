package com.github.zoewithabang.task;

import com.github.zoewithabang.util.Logging;
import org.slf4j.Logger;

public interface ITask extends Runnable
{
    Logger LOGGER = Logging.getLogger();
}
