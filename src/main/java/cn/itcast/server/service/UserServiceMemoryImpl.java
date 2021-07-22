package cn.itcast.server.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author wddv587
 *
 * UserService的简单实现
 */
public class UserServiceMemoryImpl implements UserService {
    /**
     * 存放用户数据 模拟内存数据库
     */
    private Map<String, String> allUserMap = new ConcurrentHashMap<>();

    // 在类加载的时候将用户数据存入map
    {
        allUserMap.put("zhangsan", "123");
        allUserMap.put("lisi", "123");
        allUserMap.put("wangwu", "123");
        allUserMap.put("zhaoliu", "123");
        allUserMap.put("qianqi", "123");
        allUserMap.put("liwenhao","liwenhao");
    }

    @Override
    public boolean login(String username, String password) {
        String pass = allUserMap.get(username);
        if (pass == null) {
            return false;
        }
        return pass.equals(password);
    }
}
