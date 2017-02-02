package net.navagraha.hunter.dao;

import java.util.List;

import org.hibernate.Session;

/**
 * 功能描述：持久化操作接口
 * 
 * @author 冉椿林
 *
 */
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
	 * @param _cls
	 * @param _id
	 * @return
	 */

	public Object getObjectById(Class _cls, int _id);

	/**
	 * 功能：保存或修改对象
	 * 
	 * @param _obj
	 * @return
	 */
	public void saveOrUpdate(Object _obj);

	/**
	 * 功能：修改对象
	 * 
	 * @param _obj
	 * @return
	 */
	public void update(Object _obj);

	/**
	 * 功能：根据HQL执行增删改语句
	 * 
	 * @param _hql
	 * @return
	 */
	public void executeUpdate(String _hql);

	/**
	 * 功能：添加对象
	 * 
	 * @param _obj
	 */
	public void save(Object _obj);

	/**
	 * 功能： 删除对象
	 * 
	 * @param _obj
	 */
	public void delete(Object _obj);

	/**
	 * 功能：删除对象通过id
	 * 
	 * @param _cls
	 * @param _id
	 */
	public void delete(Class _cls, int _id);

	/**
	 * 功能：根据一个字段获取对象列表
	 * 
	 * @param _table
	 * @param _dbfield
	 *            数据库字段
	 * @param _field
	 *            参数
	 * @return
	 */
	public List<?> getObjectListByfield(String _table, String _dbfield,
			Object _field);

	/**
	 * 功能：根据一个字段获取对象列表
	 * 
	 * @param _table
	 * @param _dbfield
	 *            数据库字段
	 * @param _field
	 *            参数
	 * @return
	 */
	public List<?> getObjectListByfieldInActivate(String _table,
			String _dbfield, Object _field);

	/**
	 * 功能：根据一组字段获取对象列表
	 * 
	 * @param _table
	 * @param dbfield
	 *            数据库字段数组
	 * @param fields
	 *            参数数组
	 * @return
	 */
	public List<?> getObjectListByfield(String _table, String dbfields[],
			Object fields[]);

	/**
	 * 功能：根据搜索条件获得对象列表
	 * 
	 * @param _table
	 * @param _cond
	 *            条件(需含where关键字)
	 * @return
	 */
	public List<?> getObjectListBycond(String _table, String _cond);

	/**
	 * 功能：根据搜索条件获得对象列表
	 * 
	 * @param _cond
	 *            条件(需含where关键字)
	 * @return
	 */
	public List<?> getObjectListBycond(String _cond);

	/**
	 * 功能：根据搜索条件获得对象数量
	 * 
	 * @param _cond
	 *            条件(需含where关键字)
	 * @return
	 */
	public int getObjectSizeBycond(String _cond);

	/**
	 * 功能：根据搜索条件获得对象数量(给定返回个数)
	 * 
	 * @param _cond
	 *            条件(需含where关键字)
	 * @param _limit
	 *            限制
	 * @return
	 */
	public List<?> getSomeObjectListBycond(String _cond, int _limit);

	/**
	 * 功能：根据搜索条件获得对象数量,含in
	 * 
	 * @param _cond
	 *            条件(需含where关键字)
	 * @param list
	 * @return
	 */
	public int getObjectSizeBycond(String _cond, List<?> list);

	/**
	 * 功能：登录验证
	 * 
	 * @param _table
	 * @param _username
	 * @param _password
	 * @return 验证成功/失败 = true/false
	 */
	public List<?> check4List(String _table, String _username, String _password);

	/**
	 * 功能：分页显示
	 * 
	 * @param _table
	 * @param _first
	 *            起始页
	 * @param _perPageRow
	 *            每页显示数
	 * @return
	 */
	public List<?> pageList(String _table, int _first, int _perPageRow);

	/**
	 * 功能：分页显示(含条件，需含where关键字)
	 * 
	 * @param _table
	 * @param _first
	 *            起始页
	 * @param _perPageRow
	 *            每页显示数
	 * @param _cond
	 *            条件
	 * @return
	 */
	public List<?> pageListWithCond(String _table, int _first, int _perPageRow,
			String _cond);

	/**
	 * 功能：分页显示，含in
	 * 
	 * @param _table
	 * @param _first
	 *            起始页
	 * @param _perPageRow
	 *            每页显示数
	 * @param _cond
	 *            条件,条件中应包含in(:list)
	 * @param list
	 *            in关键字后面的集合
	 * @return
	 */
	public List<?> pageListWithCond(String _table, int _first, int _perPageRow,
			String _cond, List<?> list);
}