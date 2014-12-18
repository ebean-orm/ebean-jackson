package org.avaje.ebeanorm.jackson;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.example.domain.Customer;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.*;

public class BeanJsonSerialiserTest extends AgentLoader {

  @Test
  public void testSerialize() throws Exception {


    JsonContext jsonContext = Ebean.json();

    CommonBeanSerializer jsonSerialiser = new CommonBeanSerializer(jsonContext);

    Customer customer = new Customer();
    customer.setId(42L);
    customer.setName("rob");
    customer.setVersion(1L);

    JsonFactory jsonFactory = new JsonFactory();

    StringWriter writer = new StringWriter();
    JsonGenerator generator = jsonFactory.createGenerator(writer);


    jsonSerialiser.serialize(customer, generator, null);

    generator.flush();
    generator.close();

    assertTrue(writer.toString().contains("{\"id\":42,"));

  }
}