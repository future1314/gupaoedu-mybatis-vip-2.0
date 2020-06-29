package com.gupaoedu.mybatis.gp.executor;

import com.gupaoedu.mybatis.gp.config.GpConfiguration;
import com.gupaoedu.mybatis.gp.config.MapperRegistory;
import com.gupaoedu.mybatis.gp.statement.StatementHandler;
import sun.misc.BASE64Encoder;
import sun.security.provider.MD5;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class CachingExecutor implements Executor {
    private GpConfiguration configuration;

    private SimpleExecutor delegate;

    private Map<String,Object> localCache = new HashMap();

    public CachingExecutor(SimpleExecutor delegate) {
        this.delegate = delegate;
    }

    public CachingExecutor(GpConfiguration configuration) {
        this.configuration = configuration;
    }

    public <E> E query(MapperRegistory.MapperData mapperData, Object parameter)
            throws Exception {
        //初始化StatementHandler --> ParameterHandler --> ResultSetHandler
        StatementHandler handler = new StatementHandler(configuration);
        Object result = localCache.get(sqlKey(mapperData.getSql(),parameter));
        if( null != result){
            System.out.println("缓存命中");
            return (E)result;
        }
        result =  (E) delegate.query(mapperData,parameter);
        localCache.put(sqlKey(mapperData.getSql(),parameter),result);//key 应该是sql加参数
        return (E)result;
    }

    private String sqlKey(String sql,Object parameter){
        String newstr =null;
        try {
            //确定算法
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            BASE64Encoder base64en = new BASE64Encoder();
            //加密后的字符串
            newstr = base64en.encode(md5.digest((sql + (parameter==null?"":parameter.hashCode())).getBytes("utf-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newstr;
    }
}