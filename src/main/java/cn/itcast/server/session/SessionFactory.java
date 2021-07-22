package cn.itcast.server.session;

/**
 * @author wddv587
 *
 * Session工厂 返回Session的实现类
 */
public abstract class SessionFactory {

    private static Session session = new SessionMemoryImpl();

    public static Session getSession() {
        return session;
    }
}
