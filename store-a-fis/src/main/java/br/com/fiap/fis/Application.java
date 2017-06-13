/**
 *  Copyright 2005-2016 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package br.com.fiap.fis;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import com.mongodb.DBObject;

import br.com.fiap.fis.model.Product;
import br.com.fiap.fis.processor.MongoToProductProcessor;

/**
 * A spring-boot application that includes a Camel route builder to setup the Camel routes
 */
@SpringBootApplication
@ImportResource({"classpath:spring/camel-context.xml"})
public class Application extends RouteBuilder {

    // must have a main method spring-boot can run
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean camelServletRegistrationBean() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new CamelHttpTransportServlet(), "/api/v1/*");
        registration.setName("CamelServlet");
        return registration;
    }
    
    @Override
    public void configure() throws Exception {
    	restConfiguration()
    		.component("servlet")
			.dataFormatProperty("prettyPrint", "true")
			.enableCORS(true)
			.bindingMode(RestBindingMode.json)
			.apiContextPath("/api-doc")
				.apiProperty("api.title", "Product API").apiProperty("api.version", "1")
				.apiProperty("cors", "true");
    	
    	rest("/product").id("store-a-rest-product")
			.get().description("List all products")
				.produces("application/json")
				.to("direct:listProducts")
			.get("/{upc}").description("Retrieve product")
				.produces("application/json")
				.to("direct:product");
		
		from("direct:product").id("store-a-direct-product")
			.log(LoggingLevel.INFO, "FindByUPC Header='${header.upc}'")
			.transform().simple("{ \"upc\": \"${header.upc}\" }")
			.convertBodyTo(DBObject.class)
			.to("mongodb:mongoBean?database=fiap-db-1&collection=product&operation=findOneByQuery")
			.process(new MongoToProductProcessor(true))
			.log(LoggingLevel.INFO, "Body=${body}")
			.endRest();
	
		JacksonDataFormat jsonListDF = new JacksonDataFormat();
		jsonListDF.setUnmarshalType(Product.class);
	
		from("direct:listProducts").id("store-a-direct-list-product")
		    .log(LoggingLevel.INFO, "FindAll")
			.to("mongodb:mongoBean?database=fiap-db-1&collection=product&operation=findAll")
			.process(new MongoToProductProcessor(false))
			.log(LoggingLevel.INFO, "Body=${body.toString()}")
			.endRest();
    }
}
