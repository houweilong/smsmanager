package com.njust.smsmanager.dbUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

public class DBManager {
    private static DBManager mInstance;
    private DaoMaster.DevOpenHelper openHelper;
    private final Context context;
    private final String dbName;

    private DBManager(Context context,String dbName)
    {
        this.context = context;
        this.dbName = dbName;
        openHelper = new DaoMaster.DevOpenHelper(context,dbName,null);
    }

    /**
     * 获取单例引用
     * @param context
     * @param dbName
     * @return
     */
    public static DBManager getInstance(Context context,String dbName)
    {
        if(null == mInstance)
        {
            synchronized (DBManager.class)
            {
                if(null == mInstance)
                {
                    mInstance = new DBManager(context,dbName);
                }
            }
        }
        return mInstance;
    }

    /**
     * 获取可读数据库
     * @return
     */
    private SQLiteDatabase getReadableDatabase()
    {
        if(null == openHelper)
        {
            openHelper = new DaoMaster.DevOpenHelper(context,dbName,null);
        }
        return openHelper.getReadableDatabase();
    }

    /**
     * 获取可写数据库
     * @return
     */
    private SQLiteDatabase getWritableDatabase()
    {
        if(null == openHelper)
        {
            openHelper = new DaoMaster.DevOpenHelper(context,dbName,null);
        }
        return openHelper.getWritableDatabase();
    }

    /**
     * 添加群组
     * @param name
     */
    public void createGroup(String name)
    {
        Groups groups = new Groups();
        groups.setName(name);
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        GroupsDao groupsDao = daoSession.getGroupsDao();
        groupsDao.insert(groups);
    }

    /**
     * 新创建群组时，判断输入的群组名称是否已经存在，如果存在返回true，否则返回false
     * @param name
     * @return
     */
    public boolean isExist(String name)
    {
        List<Groups> list = getAllGroups();
        for(Groups groups : list)
        {
            if(groups.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }
    /**
     * 查询群组列表
     * @return
     */
    public List<Groups> getAllGroups()
    {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        GroupsDao groupsDao = daoSession.getGroupsDao();
        QueryBuilder<Groups> queryBuilder = groupsDao.queryBuilder();
        return queryBuilder.list();
    }

    /**
     * 根据群组的ID删除指定的群组
     * @param id
     */
    public void deleteGroupById(long id)
    {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        GroupsDao groupsDao = daoSession.getGroupsDao();
        groupsDao.deleteByKey(id);
    }

    /**
     * 更新指定群组的名称
     * @param id  群组ID
     * @param name 新的名称
     */
    public void updateGroupNameById(long id,String name)
    {
        Groups groups = new Groups(id,name);
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        GroupsDao groupsDao = daoSession.getGroupsDao();
        groupsDao.update(groups);
    }

    /**
     * 根据群组id查询群组的名称
     * @param id
     * @return
     */
    public String getGroupNameById(long id)
    {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        GroupsDao groupsDao = daoSession.getGroupsDao();
        QueryBuilder<Groups> queryBuilder = groupsDao.queryBuilder().where(
                GroupsDao.Properties.Id.eq(id));
        Groups groups = queryBuilder.list().get(0);
        return groups.getName();
    }

    /**
     * 将会话ID与对应的群组ID，添加至，会话群组对应的关系表
     * @param threadId 会话ID
     * @param groupId  群组ID
     */
    public void addThreadGroupValue(long threadId,long groupId)
    {
        ThreadGroups threadGroups = new ThreadGroups();
        threadGroups.setThread_id(threadId);
        threadGroups.setGroup_id(groupId);
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ThreadGroupsDao dao = daoSession.getThreadGroupsDao();
        dao.insert(threadGroups);
    }

    /**
     * 根据threadId查询对应的groupId，如果不存在，则返回-1
     * @param threadId
     * @return
     */
    public long getGroupIdByThreadId(long threadId)
    {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ThreadGroupsDao dao = daoSession.getThreadGroupsDao();
        List<ThreadGroups> groupses = dao.queryBuilder().where(
                ThreadGroupsDao.Properties.Thread_id.eq(threadId)).list();
        if(groupses != null && groupses.size() > 0)
        {
            return groupses.get(0).getGroup_id();
        }
        return -1;
    }
    /**
     * 将对应的会话ID从group中删除
     * @param threadId
     */
    public void deleteThreadGroupValue(long threadId)
    {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ThreadGroupsDao dao = daoSession.getThreadGroupsDao();
        List<ThreadGroups> groupses = dao.queryBuilder().where(ThreadGroupsDao.Properties.Thread_id.eq(threadId)).list();
        if(groupses != null && groupses.size() > 0)
        {
            dao.delete(groupses.get(0));
        }
    }

    /**
     * 查询会话群组对应的关系表，返回包含相应的会话ID对应的CURSOR
     * @param groupId
     * @return
     */
    public List<ThreadGroups> getThreadIdByGroupId(long groupId)
    {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        ThreadGroupsDao dao = daoSession.getThreadGroupsDao();
        QueryBuilder<ThreadGroups> queryBuilder = dao.queryBuilder();
        queryBuilder.where(ThreadGroupsDao.Properties.Group_id.eq(groupId));
        return queryBuilder.list();
    }

}
