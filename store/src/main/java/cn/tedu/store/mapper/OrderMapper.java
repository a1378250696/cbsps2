package cn.tedu.store.mapper;

import cn.tedu.store.entity.Order;
import cn.tedu.store.entity.OrderItem;

/**
 * 处理订单数据和订单商品数据的持久层的接口
 * @author JAVA
 *
 */
public interface OrderMapper {
	
	/**
	 * 插入订单数据
	 * @param order 订单数据
	 * @return 受影响的行数
	 */
	Integer insertOrder(Order order);
	
	/**
	* 插入订单商品数据
	 * @param orderItem 订单商品数据
	 * @return 受影响的行数
	 */
	Integer insertOrderItem(OrderItem orderItem);
	
}
