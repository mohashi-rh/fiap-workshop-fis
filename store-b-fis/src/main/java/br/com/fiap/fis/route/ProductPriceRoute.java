package br.com.fiap.fis.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ProductPriceRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("timer:foo")
			.transform().constant("TEST...")
			.to("log:bar");
	}

}
