package net.navagraha.hunter.server;

import java.util.List;

import org.hibernate.Session;

public interface ObjectDao {

	/**
	 * 功能：获取session
	 * 
	 * @return
	 */
	public Session getSession();

	/**
	 * 功能：通过id查找对象
	 * 
	 * @param cls
	 * @param id
	 * @return
	 */

	public Object getObjectById(Class cls, int id);

	/**
	 * 功能：保存或修改对象
	 * 
	 * @param obj
	 * @return
	 */
	public void saveOrUpdate(Object obj);

	/**
	 * 功能：修改对象
	 * 
	 * @param obj
	 * @return
	 */
	public void update(Object obj);

	/**
	 * 功能：根据HQL执行增删改语句
	 * 
	 * @param obj
	 * @return
	 */
	public void executeUpdate(String hql);

	/**
	 * 功能：添加对象
	 * 
	 * @param obj
	 */
	public void save(Object obj);

	/**
	 * 功能： 删除对象
	 * 
	 * @param obj
	 */
	public void delete(Object obj);

	/**
	 * 功能：删除对象通过id
	 * 
	 * @param cls
	 * @param id
	 */
	public void delete(Class cls, int id);

	/**
	 * 功能：根据一个字段获取对象列表
	 * 
	 * @param table
	 * @param dbfield
	 *            数据库字段
	 * @param field
	 *            参数
	 * @return
	 */
	public List<?> getObjectListByfield(String table, String dbfield,
			Object field);

	/**
	 * 功能：根据一个字段获取对象列表
	 * 
	 * @param table
	 * @param dbfield
	 *            数据库字段
	 * @param field
	 *            参数
	 * @return
	 */
	public List<?> getObjectListByfieldInActivate(String table, String dbfield,
			Object field);

	/**
	 * 功能：根据一组字段获取对象列表
	 * 
	 * @param table
	 * @param dbfields
	 *            数据库字段数组
	 * @param fields
	 *            参数数组
	 * @return
	 */
	public List<?> getObjectListByfield(String table, String dbfields[],
			Object fields[]);

	/**
	 * 功能：根据搜索条件获得对象列表
	 * 
	 * @param table
	 * @param cond
	 *            条件(需含where关键字)
	 * @return
	 */
	public List<?> getObjectListBycond(String table, String cond);

	/**
	 * 功能：根据搜索条件获得对象列表
	 * 
	 * @param cond
	 *            条件(需含where关键字)
	 * @return
	 */
	public List<?> getObjectListBycond(String cond);

	/**
	 * 功能：根据搜索条件获得对象数量
	 * 
	 * @param cond
	 *            条件(需含where关键字)
	 * @return
	 */
	public int getObjectSizeBycond(String hql);

	/**
	 * 功能：根据搜索条件获得对象数量(给定返回个数)
	 * 
	 * @param cond
	 *            条件(需含where关键字)
	 * @return
	 */
	public List<?> getSomeObjectListBycond(String hql, int limit);

	/**
	 * 功能：根据搜索条件获得对象数量,含in
	 * 
	 * @param cond
	 *            条件(需含where关键字)
	 * @return
	 */
	public int getObjectSizeBycond(String hql, List<?> list);

	/**
	 * 功能：登录验证
	 * 
	 * @param table
	 * @param username
	 * @param password
	 * @return 验证成功/失败 = true/false
	 */
	public List<?> check4List(String table, String username, String password);

	/**
	 * 功能：分页显示
	 * 
	 * @param table
	 * @param first
	 *            起始页
	 * @param perPageRow
	 *            每页显示数
	 * @return
	 */
	public List<?> pageList(String table, int first, int perPageRow);

	/**
	 * 功能：分页显示(含条件，需含where关键字)
	 * 
	 * @param table
	 * @param first
	 *            起始页
	 * @param perPageRow
	 *            每页显示数
	 * @param cond
	 *            条件
	 * @return
	 */
	public List<?> pageListWithCond(String table, int first, int perPageRow,
			String cond);

	/**
	 * 功能：分页显示，含in
	 * 
	 * @param table
	 * @param first
	 *            起始页
	 * @param perPageRow
	 *            每页显示数
	 * @param cond
	 *            条件,条件中应包含in(:list)
	 * @param list
	 *            in关键字后面的集合
	 * @return
	 */
	public List<?> pageListWithCond(String table, int first, int perPageRow,
			String cond, List<?> list);
}