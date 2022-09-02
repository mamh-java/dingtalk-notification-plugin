package io.jenkins.plugins;

import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import hudson.EnvVars;
import jenkins.model.Jenkins;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


public class NotificationUtil {

    /**
     * 推送信息
     * @param url
     * @param data
     */
    public static String push(String url, String data) throws HttpProcessException, KeyManagementException, NoSuchAlgorithmException {
        HttpConfig httpConfig;
        HttpClient httpClient;
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if(url.startsWith("https")) {
            SSLContext sslContext = SSLContexts.custom().build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"},
                    null,
                    NoopHostnameVerifier.INSTANCE
            );
            httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
        }

        httpClient = httpClientBuilder.build();
        //普通请求
        httpConfig = HttpConfig.custom().client(httpClient).url(url).json(data).encoding("utf-8");

        String result = HttpClientUtil.post(httpConfig);
        return result;
    }

    /**
     * 获取Jenkins地址
     * @return
     */
    public static String getJenkinsUrl() {
        String jenkinsUrl = Jenkins.get().getRootUrl();
        if (jenkinsUrl != null && jenkinsUrl.length() > 0 && !jenkinsUrl.endsWith("/")) {
            jenkinsUrl = jenkinsUrl + "/";
        }
        return jenkinsUrl;
    }

    /**
     * 替换多值环境变量
     * @param val
     * @param envVars
     * @return
     */
    public static String replaceMultipleEnvValue(String val, EnvVars envVars) {
        String []vals = val.split(",");
        StringBuilder builder = new StringBuilder();
        for(String v : vals){
            v = replaceEnvValue(v, envVars);
            builder.append(v);
            builder.append(",");
        }
        if(builder.length()>0){
            builder.deleteCharAt(builder.length()-1);
        }
        return builder.toString();
    }

    /**
     * 替换环境变量
     * @param key
     * @param envVars
     * @return
     */
    public static String replaceEnvValue(String key, EnvVars envVars) {
        String val = key;
        if (key.contains("$")){
            key = key.trim();
            key = key.replaceFirst("\\$", "");
            if(key.startsWith("{") && key.endsWith("}")){
                key = key.substring(1, key.length()-2);
            }
            if(envVars.containsKey(key)){
                return envVars.get(key);
            }
            return val;
        }else {
            return key;
        }
    }

}
