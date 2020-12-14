package com.hust.netty.server;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @Author: wgl
 * @Description: netty随项目启动
 */
@WebListener
@Slf4j
public class NettyListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Netty listener startup...");
        new Thread(() -> {
            try {
                new ChatServer().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}