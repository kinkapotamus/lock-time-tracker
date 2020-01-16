package com.locktimetracker.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface SessionDAO {

    @Query("SELECT * FROM Session WHERE end_date IS NULL ORDER BY ID DESC LIMIT 1")
    public Session getCurrentSession();

    @Query("SELECT SUM(end_date - start_date) FROM Session")
    public long getTotalLockedTime();

    @Insert
    public void insert(Session sesh);

    @Update
    public void update(Session sesh);
}
