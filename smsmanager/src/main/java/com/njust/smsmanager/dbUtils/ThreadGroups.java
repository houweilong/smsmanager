package com.njust.smsmanager.dbUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.DaoException;

@Entity
public class ThreadGroups {
    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "thread_id")
    private Long thread_id;
    @Property(nameInDb = "group_id")
    private Long group_id;
    @ToOne(joinProperty = "group_id")
    private Groups groups;
    @Generated(hash = 1773135346)
    private transient Long groups__resolvedKey;
    /** Used for active entity operations. */
    @Generated(hash = 426558208)
    private transient ThreadGroupsDao myDao;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    public Long getGroup_id() {
        return this.group_id;
    }
    public void setGroup_id(Long group_id) {
        this.group_id = group_id;
    }
    public Long getThread_id() {
        return this.thread_id;
    }
    public void setThread_id(Long thread_id) {
        this.thread_id = thread_id;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 921423640)
    public void setGroups(Groups groups) {
        synchronized (this) {
            this.groups = groups;
            group_id = groups == null ? null : groups.getId();
            groups__resolvedKey = group_id;
        }
    }
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1062655783)
    public Groups getGroups() {
        Long __key = this.group_id;
        if (groups__resolvedKey == null || !groups__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            GroupsDao targetDao = daoSession.getGroupsDao();
            Groups groupsNew = targetDao.load(__key);
            synchronized (this) {
                groups = groupsNew;
                groups__resolvedKey = __key;
            }
        }
        return groups;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1232557499)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getThreadGroupsDao() : null;
    }
    @Generated(hash = 2120032580)
    public ThreadGroups(Long id, Long thread_id, Long group_id) {
        this.id = id;
        this.thread_id = thread_id;
        this.group_id = group_id;
    }

    public ThreadGroups(){}
}
