package cn.tedu.store.service.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.tedu.store.entity.Cart;
import cn.tedu.store.entity.Product;
import cn.tedu.store.mapper.CartMapper;
import cn.tedu.store.service.ICartService;
import cn.tedu.store.service.IProductService;
import cn.tedu.store.service.ex.AccessDeniedException;
import cn.tedu.store.service.ex.CartNotFoundException;
import cn.tedu.store.service.ex.DeleteException;
import cn.tedu.store.service.ex.UpdateException;
import cn.tedu.store.vo.CartVO;

/**
 * 
 * @author JAVA
 *
 */
@Service
public class CartServiceImpl implements ICartService {

	@Autowired
	private CartMapper cartMapper;

	@Autowired
	private IProductService productService;

	@Override
	public void addToCart(Integer uid, String username, Integer pid, Integer amount) {

			// 创建当前时间对象now(new Date())
		Date date = new Date();
			// 调用当前业务实现类的私有findByUidAndPid(Integer uid, Integer pid)方法执行查询
		Cart reault = findByUidAndPid(uid, pid);
			// 判断查询结果是否为null
		if(reault == null) {
		
			// 是：找不到数据，表示该用户的购物车中没有该商品，则需要插入新的记录
			// -- 调用productService的getById(Integer id)方法，根据参数pid查询商品数据，从中得到商品单价
			Product product = productService.getById(pid);
			
			// -- 创建新的Cart对象
		Cart cart = new Cart();
			//补全数据：uid
		cart.setUid(uid);
			//补全数据：pid
		cart.setPid(pid);
			//补全数据：num(amount)
		cart.setNum(amount);
			//补全数据：price
		cart.setPrice(product.getPrice());
			//补全数据：4个日志(username, now)
		cart.setCreatedUser(username);
		cart.setCreatedTime(date);
		cart.setModifiedUser(username);
		cart.setModifiedTime(date);
			// -- 调用当前业务实现类私有的insert(Cart cart)执行插入数据
		insert(cart);
		}else {
			// 否：找到了数据，表示该用户的购物车中已有该商品，则需要修改商品数量
			// -- 从查询结果中获取cid，用于最终调用修改数量的方法
		Integer cid = reault.getCid();
			// -- 从查询结果中取出num，即原有的数量，与参数amount(增量)相加，得到新的数量，用于最终调用修改数量的方法
		Integer newNum = reault.getNum() + amount;
			// -- 调用当前业务实现类私有的updateNumByCid(Integer cid, Integer num, String modifiedUser,
		updateNumByCid(cid, newNum, username, date);	
			// Date modifiedTime)执行修改商品数量
		}
	}
	
	
	@Override
	public List<CartVO> getVOByUid(Integer uid) {

		return findVOByUid(uid);
	}
	
	
	@Override
	public Integer addNum(Integer cid, Integer uid,String username) {
		
			// 调用findByCid(Integer cid)方法，根据参数cid查询购物车数据
		Cart add = findByCid(cid);
			// 判断查询结果是否为null
		if(add == null) {
			// 是：CartNotFoundException
			throw new CartNotFoundException("尝试访问的购物车数据不存在");
		}
		
			// 使用equals()方法判断查询结果中的uid与参数uid是否不同
		if(!add.getUid().equals(uid)) {
			// 是：AccessDeniedException
			throw new AccessDeniedException("数据异常访问!");
		}
			// 从查询结果中取出原数量，加1后得到新的数量
		Integer num = add.getNum()+1;
			// TODO 可以添加一些自定义的规则，例如每个商品的数量上限是99
		if(num>20) {
			throw new UpdateException("每个 账号购买数量不予许超出20");
		}else {
			// 调用updateNumByCid(Integer cid, Integer num, String modifiedUser, Date modifiedTime)方法更新购物车中商品的数量
		updateNumByCid(cid, num, username, new Date());
		}
			// 返回新的数量
		return num;
		
	}
	
	
	@Override
	public List<CartVO> getVOByCids(Integer[] cids, Integer uid) {
		
		//执行查询数据
		List<CartVO> carts = findVOByCids(cids);
		
		// 遍历查询结果，对比uid，并移除不是当前用户的数据
		// 迭代器：是一种安全的在遍历过程中移除当前集合中元素的工具
		Iterator<CartVO> it = carts.iterator();
		while(it.hasNext()) {
			CartVO cart = it.next();
			if (!cart.getUid().equals(uid)) {
				it.remove();
			}
		}
		
		// 返回结果
		return carts;
	}
	
	
	@Override
	public void delete(Integer[] cids, Integer uid) {
		// 执行查询数据
		List<CartVO> carts = getVOByCids(cids, uid);
		// 遍历查询结果，对比uid，并移除不是当前用户的数据
				// 迭代器：是一种安全的在遍历过程中移除当前集合中元素的工具
		if(carts.size() == 0) {
			return;
		}
		Integer[] newCids = new Integer[carts.size()];
		for (int i = 0; i < newCids.length; i++) {
			newCids[i] = carts.get(i).getCid();
		}
		deleteByCids(newCids);
	}

	
	/**
	 * 插入购物车数据 
	 * @param cart 购物车数据
	 */
	private void insert(Cart cart) {
		Integer rows = cartMapper.insert(cart);
		if (rows != 1) {
			throw new UpdateException("插入购物车数据时出现未知错误，请联系客服");
		}
	}

	/**
	 * 修改购物车中商品的数量
	 * 
	 * @param cid          购物车数据的id
	 * @param num          新的数量
	 * @param modifiedUser 修改执行人
	 * @param modifiedTime 修改时间
	 */
	private void updateNumByCid(Integer cid, Integer num, String medifiedUser, Date medifiedTime) {
		Integer rows = cartMapper.updateNumByCid(cid, num, medifiedUser, medifiedTime);
		if (rows != 1) {
			throw new UpdateException("更新购物车中的商品数量时出现未知错误，请联系客服");
		}
	}

	/**
	 * 根据用户id和商品id查询购物车数据
	 * 
	 * @param uid 用户id
	 * @param pid 商品id
	 * @return 匹配的购物车数据，如果没有匹配的数据，则返回null
	 */
	private Cart findByUidAndPid(Integer uid, Integer pid) {
		
		return cartMapper.findByUidAndPid(uid, pid);
	}
	
	/**
	 * 查询某用户的购物车数据
	 * @param uid 用户的id
	 * @return 该用户的购物车数据列表
	 */
	private List<CartVO> findVOByUid(Integer uid) {
		return cartMapper.findVOByUid(uid);
	}
	
	
	/**
	 * 根据购物车数据id查询购物车详情
	 * @param cid 购物车数据id
	 * @return 匹配的购物车详情，如果没有匹配的数据，则返回null
	 */
	private Cart findByCid(Integer cid) {
		
		return cartMapper.findByCid(cid);
	}

	/**
	 * 根据多个购物车数据id查询多条购物车数据
	 * @param cids 多个购物车数据id
	 * @return 匹配的购物车数据的集合
	 */
	private List<CartVO> findVOByCids(Integer[] cids) {
		return cartMapper.findVOByCids(cids);
	}

	

	/**
	 * 根据购物车数据id，批量删除购物车中的数据
	 * @param cids 购物车数据id
	 */
	private void deleteByCids(Integer[] cids){
		Integer rows = cartMapper.deleteByCids(cids);
		if(rows < 1) {
			throw new DeleteException("删除购物车数据时出现未知错误，请联系系统管理员");
		}
	}

	
	
}
