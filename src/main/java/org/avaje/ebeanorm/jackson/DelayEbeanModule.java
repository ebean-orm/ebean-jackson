package org.avaje.ebeanorm.jackson;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.BeanCollection;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.util.Collection;

/**
 * Jackson module that get's Ebean's JsonContext via the Ebean singleton.
 * <p>
 * This has the effect of delaying obtaining Ebean initialisation typically
 * until after DI initialisation (Guice or Spring).
 * </p>
 */
public class DelayEbeanModule extends SimpleModule {

  /**
   * Construct with no params, uses Ebean singleton to get JsonContext when actually used.
   */
  public DelayEbeanModule() {
    super();
  }

  @Override
  public String getModuleName() {
    return "jackson-datatype-ebean";
  }

  /**
   * Register the Ebean specific serializers and deserializers.
   */
  @Override
  public void setupModule(SetupContext context) {
    context.addSerializers(new DelayFindSerializers());
    context.addDeserializers(new DelayFindDeserializers());
  }

  private static class DelayFindSerializers extends Serializers.Base {

    private final DelayBeanSerializer serialiser = new DelayBeanSerializer();

    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {

      if (Ebean.json().isSupportedType(type.getRawClass())) {
        return serialiser;
      }

      return null;
    }

    @Override
    public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type, BeanDescription
        beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {

      if (type.getRawClass().isAssignableFrom(BeanCollection.class)) {
        return serialiser;
      }

      return null;
    }
  }

  private static class DelayFindDeserializers extends Deserializers.Base {

    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {

      if (isSupportedType(type.getRawClass())) {
        return new BeanTypeDeserializer(Ebean.json(), type.getRawClass());
      }
      return null;
    }


    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type, DeserializationConfig config,
                                                          BeanDescription beanDesc, TypeDeserializer typeDeserializer,
                                                          JsonDeserializer<?> elementDeserializer) throws JsonMappingException {

      Class clazz = type.getContentType().getRawClass();
      if (Collection.class.isAssignableFrom(type.getRawClass()) && isSupportedType(clazz)) {
        return new BeanListTypeDeserializer(Ebean.json(), clazz);
      }

      return null;
    }

    private boolean isSupportedType(Class<?> clazz) {
      return Ebean.json().isSupportedType(clazz);
    }

  }

  private static class DelayBeanSerializer<T> extends JsonSerializer<T> {

    @Override
    public void serialize(T o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
      Ebean.json().toJson(o, jsonGenerator);
    }
  }

}
