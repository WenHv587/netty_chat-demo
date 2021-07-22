package cn.itcast.server.service;


/**
 * @author wddv587
 *
 * UserService的工厂类，返回UserService的实现
 */
public abstract class UserServiceFactory {

    private static UserService userService = new UserServiceMemoryImpl();

    public static UserService getUserService() {
        return userService;
    }
}
