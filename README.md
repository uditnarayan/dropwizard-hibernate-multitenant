# Dropwizard Hibernate MultiTenant Bundle
This project tries to solve the multitenancy problem for dropwizard from the point of keeping seperate schema or database per tenant.

This project is under development, and I have started publishing beta releases. I am developing it as part of my work requirements to support multitenancy in our exisiting dropwizard based web service application. However, if you find this interesting enough and want to contribute, please write to me at info.udit90@gmail.com.

> **Note: The current release and development code is tested with dropwizard v1.2.2.**

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
  <version>1.2.2-BETA-2</version>
</dependency>
```

## Demo
To understand the use this bundle with dropwizard, please have a look at demo application code hosted here: https://github.com/uditnarayan/dropwizard-hibernate-multitenant-example.

## How To Contribute
If your are interested, you can contribute in following manner:
 1. Try this library with other versions of dropwizard, preferably newer that 1.2.2, find out issues and report them. We can release compatible versions with other dropwizard releases.
 2. Have a look at the code and analyze my approach to solve the problem. If your have better approach or your find any issues or want ot suggest improvements, please write to me. We can colloaborate to make it better.
 3. Try adding testcases to the current project and configure maven plugins to generate a testcase report. I will enable the continuous integration through travis. 
 4. If you have any other thoughts about how can we improve things or you want me work with you on your project or you need any help, please write to me.

## Disclaimer
I cannot take complete credit of writing the code by myself. I have only attempted to modify the existing dropwizard-hibernate module and add/rewrite peice of code required to support the multitenancy use case.

Cheers !!
