package cn.tedu.store.mapper;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.tedu.store.entity.Product;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductMapperTsets {
	
	@Autowired
	private ProductMapper mapper;
	
	@Test
	public void findNewList() {
		List<Product> list = mapper.findNewList();
		System.err.println(list.size());
		for (Product product : list) {
			System.err.println(product);
		}	
	}
	
	@Test
	public void findById() {
		Integer id = 10000037;
		Product pro = mapper.findById(id);
		System.err.println(pro);
	}
	
	
}
