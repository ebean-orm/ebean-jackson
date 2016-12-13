package io.ebean.jackson;

import io.ebean.Ebean;
import io.ebean.text.json.JsonContext;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson module that uses Ebean's JsonContext for serializing and deserializing entity beans.
 */
public class JacksonEbeanModule extends SimpleModule {

  final JsonContext jsonContext;

  /**
   * Construct with a JsonContext obtained from an EbeanServer.
   */
  public JacksonEbeanModule(JsonContext jsonContext) {
    this.jsonContext = jsonContext;
  }

  /**
   * Construct using the JsonContext from the default EbeanServer.
   */
  public JacksonEbeanModule(){
    // delay getting the jsonContext until setupModule
    this.jsonContext = null;
  }

  @Override
  public String getModuleName() {
    return "jackson-datatype-ebean";
  }

  /**
   * Register the Ebean specific serialisers and deserialisers.
   */
  @Override
  public void setupModule(SetupContext context) {
    JsonContext jc = (jsonContext != null) ? jsonContext : Ebean.json();
    context.addSerializers(new FindSerializers(jc));
    context.addDeserializers(new FindDeserializers(jc));
  }

}
