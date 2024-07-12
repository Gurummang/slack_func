package com.GASB.slack_func.repository.activity;

import com.GASB.slack_func.entity.Activities;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileActivityRepo extends JpaRepository<Activities, Long>, FileActivityCustom{
}
