package net.navagraha.hunter.server.impl;

import java.util.ArrayList;
import java.util.List;

import net.navagraha.hunter.server.HibernateSessionFactory;
import net.navagraha.hunter.server.ObjectDao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class ObjectDaoImpl implements ObjectDao {

	private SessionFactory sessionFactory = HibernateSessionFactory
			.getSessionFactory();
	private Session session;
	private Transaction tran;

	// 通过id查找对象
	@SuppressWarnings("unchecked")
	public Object getObjectById(Class cls, int id) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Object obj = session.get(cls, id);
			tran.commit();
			return obj;
		} catch (Exception e) {
			tran.rollback();
			return null;
		} finally {
			session.close();
		}
	}

	// 保存或修改对象
	public void saveOrUpdate(Object obj) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			session.saveOrUpdate(obj);
			tran.commit();
		} catch (Exception e) {
			tran.rollback();
		} finally {
			session.close();
		}
	}

	// 修改对象
	public void update(Object obj) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			session.update(obj);
			tran.commit();
		} catch (Exception e) {
			tran.rollback();
		} finally {
			session.close();
		}
	}

	// 添加对象
	public void save(Object obj) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			session.save(obj);
			tran.commit();
		} catch (Exception e) {
			tran.rollback();
		} finally {
			session.close();
		}
	}

	// 删除对象
	public void delete(Object obj) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			session.delete(obj);
			tran.commit();
		} catch (Exception e) {
			tran.rollback();
		} finally {
			session.close();
		}
	}

	// 删除对象通过id
	@SuppressWarnings("unchecked")
	public void delete(Class cls, int id) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			session.delete(session.load(cls, id));
			tran.commit();
		} catch (Exception e) {
			tran.rollback();
		} finally {
			session.close();
		}
	}

	// 根据一个字段获取对象列表
	public List<?> getObjectListByfield(String table, String dbfield,
			Object field) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			String hql = "from " + table + " where " + dbfield + "=?";
			if (table.equals("Users")) {
				hql += " and useIscompany<>2";
			}
			Query query = session.createQuery(hql);
			query.setParameter(0, field);
			query.setCacheable(true);// 使用二级缓存
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			tran.rollback();
			return new ArrayList<Object>();
		} finally {
			session.close();
		}
	}

	// 根据一个字段获取对象列表(激活使用)
	public List<?> getObjectListByfieldInActivate(String table, String dbfield,
			Object field) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			String hql = "from " + table + " where " + dbfield + "=?";

			Query query = session.createQuery(hql);
			query.setParameter(0, field);
			query.setCacheable(true);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			tran.rollback();
			return new ArrayList<Object>();
		} finally {
			session.close();
		}
	}

	// 根据一组字段获取对象列表（参数数组应小于3，2以上为写相应代码）
	public List<?> getObjectListByfield(String table, String dbfields[],
			Object fields[]) {
		String hql = "from " + table + " where ";
		for (int i = 0; i < dbfields.length; i++) {
			if (i == fields.length - 1) {
				hql += dbfields[i] + "=?";
				continue;
			}
			hql += dbfields[i] + "=? and ";
		}
		if (table.equals("Users")) {
			hql += " and useIscompany<>2";
		}
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(hql);
			for (int i = 0; i < dbfields.length; i++) {
				query.setParameter(i, fields[i]);
			}
			query.setCacheable(true);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			tran.rollback();
			return new ArrayList<Object>();
		} finally {
			session.close();
		}
	}

	// 根据搜索条件获得对象数
	public int getObjectSizeBycond(String hql, List<?> list) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(hql);
			query.setParameterList("list", list);
			int i = ((Long) query.iterate().next()).intValue();
			tran.commit();
			return i;
		} catch (Exception e) {
			tran.rollback();
			return 0;
		} finally {
			session.close();
		}
	}

	// 根据搜索条件获得对象数
	public int getObjectSizeBycond(String hql) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(hql);
			int i = ((Long) query.iterate().next()).intValue();
			tran.commit();
			return i;
		} catch (Exception e) {
			tran.rollback();
			return 0;
		} finally {
			session.close();
		}
	}

	// 根据搜索条件获得对象列表
	public List<?> getObjectListBycond(String table, String cond) {
		String hql = "from " + table + " " + cond;
		if (table.equals("Users")) {
			hql += " and useIscompany<>2";
		}
		return getObjectListBycond(hql);

	}

	// 根据搜索条件获得对象列表
	public List<?> getObjectListBycond(String cond) {
		String hql = cond;
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(hql);
			query.setCacheable(true);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			tran.rollback();
			return new ArrayList<Object>();
		} finally {
			session.close();
		}
	}

	// 登录验证
	public List<?> check4List(String table, String usePhone, String password) {
		String hql = "from " + table
				+ " where usePhone = ? and usePwd = ? and useIscompany<>2";
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			Query query = session.createQuery(hql).setString(0, usePhone)
					.setString(1, password);
			query.setCacheable(true);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			tran.rollback();
			return new ArrayList<Object>();
		} finally {
			session.close();
		}
	}

	// 分页显示
	public List<?> pageList(String table, int first, int perPageRow) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			String hql = "from " + table;
			if (table.equals("Users")) {
				hql += "where useIscompany<>2";
			}
			Query query = session.createQuery(hql);
			query.setFirstResult(first * perPageRow);
			query.setMaxResults(perPageRow);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			tran.rollback();
			return new ArrayList<Object>();
		} finally {
			session.close();
		}
	}

	// 分页显示(含条件，需含where关键字)
	public List<?> pageListWithCond(String table, int first, int perPageRow,
			String cond) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			String hql = "from " + table + " " + cond;
			if (table.equals("Users")) {
				hql += " and useIscompany<>2";
			}
			Query query = session.createQuery(hql);
			query.setFirstResult(first * perPageRow);
			query.setMaxResults(perPageRow);
			List<?> list = query.list();
			tran.commit();
			return list;
		} catch (Exception e) {
			tran.rollback();
			return new ArrayList<Object>();
		} finally {
			session.close();
		}
	}

	// 分页显示(含条件，需含where关键字)
	public List<?> pageListWithCond(String table, int first, int perPageRow,
			String cond, List<?> list) {
		try {
			session = sessionFactory.openSession();
			tran = session.beginTransaction();
			String hql = "from " + table + " " + cond;
			if (table.equals("Users")) {
				hql += " and useIscompany<>2";
			}
			Query query = session.createQuery(hql);
			query.setFirstResult(first * perPageRow);
			query.setMaxResults(perPageRow);
			query.setParameterList("list", list);
			List<?> list1 = query.list();
			tran.commit();
			return list1;
		} catch (Exception e) {
			tran.rollback();
			return new ArrayList<Object>();
		} finally {
			session.close();
		}
	}

}
