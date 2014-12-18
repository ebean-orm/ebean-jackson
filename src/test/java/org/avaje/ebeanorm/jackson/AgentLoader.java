package org.avaje.ebeanorm.jackson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentLoader {
  
  protected static Logger logger = LoggerFactory.getLogger(AgentLoader.class);
  
  static {
    logger.debug("... preStart");
    if (!org.avaje.agentloader.AgentLoader.loadAgentFromClasspath("avaje-ebeanorm-agent", "debug=9;packages=org.example.**")) {
      logger.info("avaje-ebeanorm-agent not found in classpath - not dynamically loaded");
    }    
  }

  public static void touch() {
    // do nothing
  }

}