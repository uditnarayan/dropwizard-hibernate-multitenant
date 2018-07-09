# Dropwizard Hibernate MultiTenant Bundle
[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.uditnarayan.dropwizard/dropwizard-hibernate-multitenant.svg)](https://clojars.org/org.clojars.uditnarayan.dropwizard/dropwizard-hibernate-multitenant)

This project tries to solve the multitenancy problem for dropwizard from the point of keeping seperate schema or database per tenant.

This project is under development, and I have started publishing beta releases. I am developing it as part of my work requirements to support multitenancy in our exisiting dropwizard based web service application. However, if you find this interesting enough and want to contribute, please write to me at info.udit90@gmail.com.

## Usage
This library is available in clojars. Please use the following repository with maven:

```
<repository>
    <id>clojars</id>
    <name>Clojars repository</name>
    <url>https://clojars.org/repo</url>
</repository>
```

The dependency config is:
```
<dependency>
  <groupId>org.clojars.uditnarayan.dropwizard</groupId>
  <artifactId>dropwizard-hibernate-multitenant</artifactId>
  <version>1.2.2-BETA-1</version>
</dependency>
```


## Demo
To understand the use this bundle with dropwizard, please have a look at demo application code hosted here: https://github.com/uditnarayan/dropwizard-hibernate-multitenant-example.

## Disclaimer
I cannot take complete credit of writing the code by myself. I have only attempted to modify the existing dropwizard-hibernate module and add/rewrite peice of code required to support the multitenancy use case.

Cheers !!
