package com.hxy.component.commandLineRunner;

import com.hxy.modules.common.utils.RedisClusterUtil;
import com.hxy.modules.sys.dao.CodeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 类的功能描述.
 * 服务启动执行
 * @Auther hxy
 * @Date 2017/11/14
 */
@Component
public class MyStartupRunner implements CommandLineRunner {

    @Autowired
    private CodeDao codeDao;

    @Autowired
    private RedisClusterUtil redisClusterUtil;

    @Override
    public void run(String... strings) throws Exception {
        System.out.println("服务启动执行服务启动执行服务启动执行服务启动执行");
        //CodeCache();
    }


}
