package io.ebean.jackson;

import io.ebean.Ebean;
import io.ebean.text.json.JsonContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.AWrapperBean;
import org.example.domain.AWrapperBeanList;
import org.example.domain.Customer;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class JacksonEbeanModuleTest {

  @Test
  public void test() throws IOException {

    ObjectMapper mapper = new ObjectMapper();
    JsonContext jsonContext = Ebean.json();

    mapper.registerModule(new JacksonEbeanModule(jsonContext));

    Customer customer = createCustomer();


    StringWriter writer = new StringWriter();
    JsonGenerator generator = mapper.getFactory().createGenerator(writer);

    mapper.writeValue(generator, customer);

    generator.flush();
    generator.close();

    String jsonContent = writer.toString();
    System.out.println("JsonContent: " + jsonContent);
    assertTrue(jsonContent.contains("{\"id\":42,"));

    // process again to see Jackson caching the serialiser
    StringWriter writer2 = new StringWriter();
    JsonGenerator generator2 = mapper.getFactory().createGenerator(writer2);
    mapper.writeValue(generator2, customer);
    generator2.flush();
    generator2.close();

    assertEquals(jsonContent, writer2.toString());

    // read via Ebean's JsonContext
    Customer customer2 = jsonContext.toBean(Customer.class, jsonContent);
    assertEquals(customer.getId(), customer2.getId());
    assertEquals(customer.getName(), customer2.getName());
    assertEquals(customer.getVersion(), customer2.getVersion());


    // read via Jackson module
    Customer customer1 = mapper.readValue(jsonContent, Customer.class);

    assertEquals(customer.getId(), customer1.getId());
    assertEquals(customer.getName(), customer1.getName());
    assertEquals(customer.getVersion(), customer1.getVersion());


    String jsonWithUnknown = "{\"id\":42,\"unknownProp\":\"foo\",\"name\":\"rob\",\"version\":1}";

    Customer customer3 = jsonContext.toBean(Customer.class, jsonWithUnknown);
    assertEquals(customer.getId(), customer3.getId());
    assertEquals(customer.getName(), customer3.getName());
    assertEquals(customer.getVersion(), customer3.getVersion());
  }

  @Test
  public void test_beanWrapping() throws IOException {

    AWrapperBean wrapperBean = new AWrapperBean();
    wrapperBean.good = "hello";
    wrapperBean.stuff = 42;
    wrapperBean.customer = createCustomer();

    ObjectMapper mapper = new ObjectMapper();
    JsonContext jsonContext = Ebean.json();
    mapper.registerModule(new JacksonEbeanModule(jsonContext));

    StringWriter writer = new StringWriter();
    JsonGenerator generator = mapper.getFactory().createGenerator(writer);
    mapper.writeValue(generator, wrapperBean);
    generator.flush();
    generator.close();

    String jsonContent = writer.toString();
    System.out.println("json wrapperBean: "+jsonContent);
    assertTrue(jsonContent.contains("{\"good\":\"hello\","));
    assertTrue(jsonContent.contains("\"stuff\":42,"));
    assertTrue(jsonContent.contains(",\"customer\":{\"id\":"));


    AWrapperBean wrapperBean1 = mapper.readValue(jsonContent, AWrapperBean.class);

    assertEquals(wrapperBean.good, wrapperBean1.good);
    assertEquals(wrapperBean.stuff, wrapperBean1.stuff);
    assertEquals(wrapperBean.customer.getId(), wrapperBean1.customer.getId());
    assertEquals(wrapperBean.customer.getName(), wrapperBean1.customer.getName());
    assertEquals(wrapperBean.customer.getVersion(), wrapperBean1.customer.getVersion());
  }

  @Test
  public void test_beanListWrapping() throws IOException {

    AWrapperBeanList wrapperBeanList = new AWrapperBeanList();
    wrapperBeanList.good = "hello";
    wrapperBeanList.stuff = 42;
    List<Customer> customers = new ArrayList<Customer>();
    wrapperBeanList.customers = customers;

    customers.add(createCustomer(1L,"one"));
    customers.add(createCustomer(2L, "two"));

    ObjectMapper mapper = new ObjectMapper();
    JsonContext jsonContext = Ebean.json();
    mapper.registerModule(new JacksonEbeanModule(jsonContext));

    StringWriter writer = new StringWriter();
    JsonGenerator generator = mapper.getFactory().createGenerator(writer);
    mapper.writeValue(generator, wrapperBeanList);
    generator.flush();
    generator.close();

    String jsonContent = writer.toString();
    System.out.println("json wrapperBeanList: "+jsonContent);
    assertTrue(jsonContent.contains("{\"good\":\"hello\","));
    assertTrue(jsonContent.contains("\"stuff\":42,"));
    assertTrue(jsonContent.contains(",\"customers\":[{\"id\":"));


    AWrapperBeanList wrapperBeanList1 = mapper.readValue(jsonContent, AWrapperBeanList.class);

    assertEquals(wrapperBeanList.good, wrapperBeanList1.good);
    assertEquals(wrapperBeanList.stuff, wrapperBeanList1.stuff);
    assertEquals(wrapperBeanList.customers.size(), wrapperBeanList1.customers.size());
    assertEquals(wrapperBeanList.customers.get(0).getName(), wrapperBeanList1.customers.get(0).getName());
    assertEquals(wrapperBeanList.customers.get(1).getName(), wrapperBeanList1.customers.get(1).getName());
  }

  private Customer createCustomer() {
    Customer customer = new Customer();
    customer.setId(42L);
    customer.setName("rob");
    customer.setVersion(1L);
    return customer;
  }
  private Customer createCustomer(long id, String name) {
    Customer customer = new Customer();
    customer.setId(id);
    customer.setName(name);
    customer.setVersion(1L);
    return customer;
  }
}