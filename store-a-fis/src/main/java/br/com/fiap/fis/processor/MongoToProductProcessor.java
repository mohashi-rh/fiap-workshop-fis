package br.com.fiap.fis.processor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.mongodb.BasicDBObject;

import br.com.fiap.fis.model.Product;

public class MongoToProductProcessor implements Processor {

	private final Boolean single;
	
	public MongoToProductProcessor(Boolean single) {
		this.single = single;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void process(Exchange exchange) throws Exception {
		if (single) {
			BasicDBObject mongoProduct = exchange.getIn().getBody(BasicDBObject.class);
			if (mongoProduct != null) {
				Product product = toProduct(mongoProduct);
				exchange.getIn().setBody(product);
			}
		} else {
			List<BasicDBObject> mongoProductList = exchange.getIn().getBody(List.class);
			if (mongoProductList != null) {
				List<Product> productList = new ArrayList<Product>();
				for (BasicDBObject mongoProduct : mongoProductList) {
					Product product = toProduct(mongoProduct);
					productList.add(product);
				}
				exchange.getIn().setBody(productList);
			}
		}
	}

	private Product toProduct(BasicDBObject mongoProduct) {
		Product product = new Product();
		product.setId(mongoProduct.getString("_id"));
		product.setPrice(new BigDecimal(mongoProduct.getString("price")));
		product.setQuantity(new BigInteger(mongoProduct.getString("quantity")));
		product.setUpc(mongoProduct.getString("upc"));
		return product;
	}

}
