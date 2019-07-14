package com.fuxi.core.common.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import com.fuxi.core.common.dao.ICommonDao;
import com.fuxi.core.common.hibernate.qbc.CriteriaQuery;
import com.fuxi.core.common.hibernate.qbc.HqlQuery;
import com.fuxi.core.common.hibernate.qbc.PageList;
import com.fuxi.core.common.model.json.DataGridReturn;
import com.fuxi.core.common.service.CommonService;
import com.fuxi.system.util.MyBeanUtils;
import com.fuxi.tag.vo.datatable.DataTableReturn;

/**
 * Title: CommonServiceImpl Description: CommonService公共接口实现类
 * 
 * @author LJ
 * 
 */
@Service("commonService")
@Transactional
public class CommonServiceImpl implements CommonService {
    public ICommonDao commonDao = null;



    public Integer getAllDbTableSize() {
        return commonDao.getAllDbTableSize();
    }

    @Resource
    public void setCommonDao(ICommonDao commonDao) {
        this.commonDao = commonDao;
    }


    public <T> Serializable save(T entity) {
        return commonDao.save(entity);
    }


    public <T> void saveOrUpdate(T entity) {
        commonDao.saveOrUpdate(entity);
    }


    public <T> void delete(T entity) {
        commonDao.delete(entity);

    }

    /**
     * 删除实体集合
     * 
     * @param <T>
     * @param entities
     */
    public <T> void deleteAllEntitie(Collection<T> entities) {
        commonDao.deleteAllEntitie(entities);
    }

    /**
     * 根据实体名获取对象
     */
    public <T> T get(Class<T> class1, Serializable id) {
        return commonDao.get(class1, id);
    }

    /**
     * 根据实体名返回全部对象
     * 
     * @param <T>
     * @param hql
     * @param size
     * @return
     */
    public <T> List<T> getList(Class clas) {
        return commonDao.loadAll(clas);
    }

    /**
     * 根据实体名获取对象
     */
    public <T> T getEntity(Class entityName, Serializable id) {
        if (id == null) {
            return null;
        }
        return commonDao.getEntity(entityName, id);
    }

    /**
     * 根据实体名称和字段名称和字段值获取唯一记录
     * 
     * @param <T>
     * @param entityClass
     * @param propertyName
     * @param value
     * @return
     */
    public <T> T findUniqueByProperty(Class<T> entityClass, String propertyName, Object value) {
        return commonDao.findUniqueByProperty(entityClass, propertyName, value);
    }

    /**
     * 按属性查找对象列表.
     */
    public <T> List<T> findByProperty(Class<T> entityClass, String propertyName, Object value) {

        return commonDao.findByProperty(entityClass, propertyName, value);
    }


    /**
     * 加载全部实体
     * 
     * @param <T>
     * @param entityClass
     * @return
     */
    public <T> List<T> loadAll(final Class<T> entityClass) {
        return commonDao.loadAll(entityClass);
    }

    public <T> T singleResult(String hql) {
        return commonDao.singleResult(hql);
    }

    /**
     * 删除实体主键ID删除对象
     * 
     * @param <T>
     * @param entities
     */
    public <T> void deleteEntityById(Class entityName, Serializable id) {
        commonDao.deleteEntityById(entityName, id);
    }

    /**
     * 更新指定的实体
     * 
     * @param <T>
     * @param pojo
     */
    public <T> void updateEntitie(T pojo) {
        commonDao.updateEntitie(pojo);

    }

    /**
     * 通过hql 查询语句查找对象
     * 
     * @param <T>
     * @param query
     * @return
     */
    public <T> List<T> findByQueryString(String hql) {
        return commonDao.findByQueryString(hql);
    }

    /**
     * 根据sql更新
     * 
     * @param query
     * @return
     */
    public int updateBySqlString(String sql) {
        return commonDao.updateBySqlString(sql);
    }

    /**
     * 根据sql查找List
     * 
     * @param <T>
     * @param query
     * @return
     */
    public <T> List<T> findListbySql(String query) {
        return commonDao.findListbySql(query);
    }

    /**
     * 通过属性称获取实体带排序
     * 
     * @param <T>
     * @param clas
     * @return
     */
    public <T> List<T> findByPropertyisOrder(Class<T> entityClass, String propertyName, Object value, boolean isAsc) {
        return commonDao.findByPropertyisOrder(entityClass, propertyName, value, isAsc);
    }

    /**
     * 
     * cq方式分页
     * 
     * @param cq
     * @param isOffset
     * @return
     */
    public PageList getPageList(final CriteriaQuery cq, final boolean isOffset) {
        return commonDao.getPageList(cq, isOffset);
    }

    /**
     * 返回DataTableReturn模型
     * 
     * @param cq
     * @param isOffset
     * @return
     */
    public DataTableReturn getDataTableReturn(final CriteriaQuery cq, final boolean isOffset) {
        return commonDao.getDataTableReturn(cq, isOffset);
    }

    /**
     * 返回easyui datagrid模型
     * 
     * @param cq
     * @param isOffset
     * @return
     */
    public DataGridReturn getDataGridReturn(final CriteriaQuery cq, final boolean isOffset) {
        return commonDao.getDataGridReturn(cq, isOffset);
    }

    /**
     * 
     * hqlQuery方式分页
     * 
     * @param cq
     * @param isOffset
     * @return
     */
    public PageList getPageList(final HqlQuery hqlQuery, final boolean needParameter) {
        return commonDao.getPageList(hqlQuery, needParameter);
    }

    /**
     * 
     * sqlQuery方式分页
     * 
     * @param cq
     * @param isOffset
     * @return
     */
    public PageList getPageListBySql(final HqlQuery hqlQuery, final boolean isToEntity) {
        return commonDao.getPageListBySql(hqlQuery, isToEntity);
    }

    public Session getSession()

    {
        return commonDao.getSession();
    }

    public List findByExample(final String entityName, final Object exampleEntity) {
        return commonDao.findByExample(entityName, exampleEntity);
    }

    /**
     * 通过cq获取全部实体
     * 
     * @param <T>
     * @param cq
     * @return
     */
    public <T> List<T> getListByCriteriaQuery(final CriteriaQuery cq, Boolean ispage) {
        return commonDao.getListByCriteriaQuery(cq, ispage);
    }


    public Integer executeSql(String sql, List<Object> param) {
        return commonDao.executeSql(sql, param);
    }


    public Integer executeSql(String sql, Object... param) {
        return commonDao.executeSql(sql, param);
    }


    public Integer executeSql(String sql, Map<String, Object> param) {
        return commonDao.executeSql(sql, param);
    }

    public Object executeSqlReturnKey(String sql, Map<String, Object> param) {
        return commonDao.executeSqlReturnKey(sql, param);
    }

    public List<Map<String, Object>> findForJdbc(String sql, int page, int rows) {
        return commonDao.findForJdbc(sql, page, rows);
    }


    public List<Map<String, Object>> findForJdbc(String sql, Object... objs) {
        return commonDao.findForJdbc(sql, objs);
    }


    public List<Map<String, Object>> findForJdbcParam(String sql, int page, int rows, Object... objs) {
        return commonDao.findForJdbcParam(sql, page, rows, objs);
    }


    public <T> List<T> findObjForJdbc(String sql, int page, int rows, Class<T> clazz) {
        return commonDao.findObjForJdbc(sql, page, rows, clazz);
    }


    public Map<String, Object> findOneForJdbc(String sql, Object... objs) {
        return commonDao.findOneForJdbc(sql, objs);
    }


    public Long getCountForJdbc(String sql) {
        return commonDao.getCountForJdbc(sql);
    }


    public Long getCountForJdbcParam(String sql, Object[] objs) {
        return commonDao.getCountForJdbc(sql);
    }


    public <T> void batchSave(List<T> entitys) {
        this.commonDao.batchSave(entitys);
    }

    /**
     * 通过hql 查询语句查找对象
     * 
     * @param <T>
     * @param query
     * @return
     */
    public <T> List<T> findHql(String hql, Object... param) {
        return this.commonDao.findHql(hql, param);
    }

    public <T> List<T> pageList(DetachedCriteria dc, int firstResult, int maxResult) {
        return this.commonDao.pageList(dc, firstResult, maxResult);
    }

    public <T> List<T> findByDetached(DetachedCriteria dc) {
        return this.commonDao.findByDetached(dc);
    }

    @Override
    public Object get(String hql) {
        Object o = null;
        List<Object> list = (List<Object>) commonDao.findByQueryString(hql);
        if (list.size() > 0) {
            o = list.get(0);
        }
        return o;
    }

    @Override
    public <T> String getName(Class<T> class1, Serializable id, String name) {
        String result = null;
        if (id == null)
            return "";
        T entity = getEntity(class1, id);
        if (entity == null)
            return "";
        try {
            result = MyBeanUtils.getProperty(entity, name);

            // }
        } catch (Exception e) {
            ReflectionUtils.handleReflectionException(e);
        }
        return result;
    }

}
