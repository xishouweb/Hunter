package net.navagraha.hunter.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * 功能描述：持久化操作实现类
 * 
 * @author 冉椿林
 *
 */
public class ObjectDaoImpl implements ObjectDao {

	private SessionFactory sessionFactory = HibernateSessionFactory
			.getSessionFactory();// session工厂

	private Session session;// hibernate_session

	private Transaction tran;// 事务

	public Session getSession() {
		return session;
	}

	// 通过id查找对象
	public Object getObjectById(Class _cls, int _id) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Object obj = session.get(_cls, _id);
			tran.commit();
			return obj;
		} catch (Exception e) {
			return null;
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 保存或修改对象
	public void saveOrUpdate(Object _obj) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			session.saveOrUpdate(_obj);
			tran.commit();
		} catch (Exception e) {
			tran.rollback();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 修改对象
	public void update(Object _obj) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			session.update(_obj);
			tran.commit();
		} catch (Exception e) {
			tran.rollback();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 根据HQL执行增删改语句
	public void executeUpdate(String _sHql) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(_sHql);
			query.executeUpdate();
			tran.commit();
		} catch (Exception e) {
			tran.rollback();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 添加对象
	public void save(Object _obj) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			session.save(_obj);
			tran.commit();
		} catch (Exception e) {
			tran.rollback();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 删除对象
	public void delete(Object _obj) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			session.delete(_obj);
			tran.commit();
		} catch (Exception e) {
			tran.rollback();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 删除对象通过id
	public void delete(Class _cls, int _id) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			session.delete(session.load(_cls, _id));
			tran.commit();
		} catch (Exception e) {
			tran.rollback();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 根据一个字段获取对象列表
	public List<?> getObjectListByfield(String _table, String _dbfield,
			Object _field) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			String sHql = "from " + _table + " where " + _dbfield + "=?";
			if (_table.equals("Users")) {
				sHql += " and useIscompany<>2";
			}
			Query query = session.createQuery(sHql);
			query.setParameter(0, _field);
			query.setCacheable(true);// 使用二级缓存
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			return new ArrayList<Object>();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 根据一个字段获取对象列表(激活使用)
	public List<?> getObjectListByfieldInActivate(String _table,
			String _dbfield, Object _field) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			String sHql = "from " + _table + " where " + _dbfield + "=?";

			Query query = session.createQuery(sHql);
			query.setParameter(0, _field);
			query.setCacheable(true);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			return new ArrayList<Object>();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 根据一组字段获取对象列表（参数数组应小于3，2以上为写相应代码）
	public List<?> getObjectListByfield(String _table, String dbfields[],
			Object fields[]) {
		String sHql = "from " + _table + " where ";
		for (int i = 0; i < dbfields.length; i++) {
			if (i == fields.length - 1) {
				sHql += dbfields[i] + "=?";
				continue;
			}
			sHql += dbfields[i] + "=? and ";
		}
		if (_table.equals("Users")) {
			sHql += " and useIscompany<>2";
		}
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(sHql);
			for (int i = 0; i < dbfields.length; i++) {
				query.setParameter(i, fields[i]);
			}
			query.setCacheable(true);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			return new ArrayList<Object>();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 根据搜索条件获得对象数
	public int getObjectSizeBycond(String _sHql, List<?> list) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(_sHql);
			query.setParameterList("list", list);
			int i = ((Long) query.iterate().next()).intValue();
			tran.commit();
			return i;
		} catch (Exception e) {
			return 0;
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 根据搜索条件获得对象数
	public int getObjectSizeBycond(String _sHql) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(_sHql);
			int i = (Long.valueOf(query.iterate().next().toString()))
					.intValue();
			tran.commit();
			return i;
		} catch (Exception e) {
			return 0;
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 根据搜索条件获得对象数量(给定返回个数)
	public List<?> getSomeObjectListBycond(String _sHql, int _limit) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(_sHql);
			query.setMaxResults(_limit);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			return new ArrayList<Object>();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 根据搜索条件获得对象列表
	public List<?> getObjectListBycond(String _table, String _cond) {
		String sHql = "from " + _table + " " + _cond;
		if (_table.equals("Users")) {
			sHql += " and useIscompany<>2";
		}
		return getObjectListBycond(sHql);

	}

	// 根据搜索条件获得对象列表
	public List<?> getObjectListBycond(String _cond) {
		String sHql = _cond;
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(sHql);
			query.setCacheable(true);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			return new ArrayList<Object>();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 登录验证
	public List<?> check4List(String _table, String _usePhone, String _password) {
		String sHql = "from " + _table
				+ " where usePhone = ? and usePwd = ? and useIscompany<>2";
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(sHql).setString(0, _usePhone)
					.setString(1, _password);
			query.setCacheable(true);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			return new ArrayList<Object>();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 分页显示
	public List<?> pageList(String _table, int _first, int _perPageRow) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			String sHql = "from " + _table;
			if (_table.equals("Users")) {
				sHql += "where useIscompany<>2";
			}
			Query query = session.createQuery(sHql);
			query.setFirstResult(_first * _perPageRow);
			query.setMaxResults(_perPageRow);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			return new ArrayList<Object>();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 分页显示(含条件，需含where关键字)
	public List<?> pageListWithCond(String _table, int _first, int _perPageRow,
			String _cond) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			String sHql = "from " + _table + " " + _cond;
			if (_table.equals("Users")) {
				sHql += " and useIscompany<>2";
			}
			Query query = session.createQuery(sHql);
			query.setFirstResult(_first * _perPageRow);
			query.setMaxResults(_perPageRow);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			return new ArrayList<Object>();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	// 分页显示(含条件，需含where关键字)
	public List<?> pageListWithCond(String _table, int _first, int _perPageRow,
			String _cond, List<?> list) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			String sHql = "from " + _table + " " + _cond;
			if (_table.equals("Users")) {
				sHql += " and useIscompany<>2";
			}
			Query query = session.createQuery(sHql);
			query.setFirstResult(_first * _perPageRow);
			query.setMaxResults(_perPageRow);
			query.setParameterList("list", list);
			List<?> list1 = query.list();
			tran.commit();
			return list1;
		} catch (Exception e) {
			return new ArrayList<Object>();
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

}
