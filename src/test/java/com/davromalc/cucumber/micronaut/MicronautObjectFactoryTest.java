package com.davromalc.cucumber.micronaut;

import io.micronaut.context.Qualifier;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MicronautObjectFactoryTest {

  MicronautObjectFactory objectFactory = new MicronautObjectFactory();

  @BeforeEach
  void setUp() {
    objectFactory.start();
  }

  @AfterEach
  void tearDown() {
    objectFactory.stop();
  }

  @Test
  void given_An_Object_When_Cucumber_Gets_An_Instance_Then_The_Bean_Is_Returned() {
    // given
    final Class<MyBean> beanType = MyBean.class;

    // when
    final MyBean myBean = objectFactory.getInstance(beanType);

    // then
    Assertions.assertNotNull(myBean);
  }

  @Test
  void given_A_Bean_With_Dependencies_Registered_Into_Micronaut_Context_When_Cucumber_Gets_An_Instance_Then_The_Bean_Is_Returned() {
    // given
    objectFactory.applicationContext.registerSingleton(new MyBean());

    // when
    final MyBeanWithDependencies myBean = objectFactory.getInstance(MyBeanWithDependencies.class);

    // then
    Assertions.assertNotNull(myBean);
    Assertions.assertDoesNotThrow(myBean::doStuff);
  }

  @Test
  void given_A_Bean_With_Dependencies_Registered_Into_Micronaut_Context_With_Named_When_Cucumber_Gets_An_Instance_Then_The_Desired_Bean_Is_Returned() {
    // given
    objectFactory.applicationContext.registerSingleton(MyBeanInterface.class, new MyBeanImplementation());
    final MyDecoratorBeanImplementation decorator = new MyDecoratorBeanImplementation(new MyBeanImplementation());
    objectFactory.applicationContext.registerSingleton(MyBeanInterface.class, decorator);

    // when
    final MyBeanClient myBean = objectFactory.getInstance(MyBeanClient.class);

    // then
    Assertions.assertNotNull(myBean);
    Assertions.assertDoesNotThrow(myBean::main);
    Assertions.assertEquals(decorator, myBean.bean);
  }

  @Test
  void given_A_Bean_With_Parameter_Types_Registered_Into_Micronaut_Context_With_Named_When_Cucumber_Gets_An_Instance_Then_The_Desired_Bean_Is_Returned() {
    // given
    final Qualifier<ParametrizedBean> qualifierForString = Qualifiers.byTypeArguments(String.class, String.class);
    objectFactory.applicationContext.registerSingleton(ParametrizedBean.class, new StringBasedParametrizesBean(), qualifierForString);
    final Qualifier<ParametrizedBean> qualifierForLongToString = Qualifiers.byTypeArguments(Long.class, String.class);
    final ParametrizedBean<Long, String> longToString = new LongToStringBasedParametrizesBean();
    objectFactory.applicationContext.registerSingleton(ParametrizedBean.class, longToString, qualifierForLongToString);

    // when
    final ParametrizesBeanClient myBean = objectFactory.getInstance(ParametrizesBeanClient.class);

    // then
    Assertions.assertNotNull(myBean);
    Assertions.assertDoesNotThrow(myBean::main);
    Assertions.assertEquals(longToString, myBean.myBean);
  }
}
