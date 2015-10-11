/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.v2.client;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.zipato.model.client.RestObject;
import com.zipato.model.client.SecurityUpdateItem;
import com.zipato.model.client.UserSessionRest;
import com.zipato.model.user.User;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.social.ResourceNotFoundException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by dbudor on 02/06/2014.
 */

public class ApiV2RestTemplate extends CookieRestTemplate {

    private static final String TAG = ApiV2RestTemplate.class.getSimpleName();
    private final ObjectMapper mapper;
    private final Object lock = new Object();
    private String localUrl;
    private String remoteUrl;
    private boolean useLocal;
    private String securitySessionId;
    private boolean authenticated;
    private String serial;
    private String username;
    private String password;
    private String local;
    private APIV2RestCallback callback;
    private volatile boolean isLoginFlag;
    private volatile boolean isLogoutFlag;
    private volatile boolean isRegFLag;
    private volatile boolean isPassRecFlag;
    private volatile boolean canReLog = true;
    // private ThreadLocal<Boolean> stateless = new ThreadLocal<Boolean>();
    private String clientSessionId;
    private String gcmToken;
    private boolean gcmRegistered;

    public ApiV2RestTemplate() {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        final ISO8601DateFormat df = new ISO8601DateFormat();
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(df);

        MappingJackson2HttpMessageConverter mc = new MappingJackson2HttpMessageConverter();
        mc.setObjectMapper(mapper);
        getMessageConverters().add(mc);
        setErrorHandler(new JsonErrorHandler(mapper));
        setInterceptors(Collections.<ClientHttpRequestInterceptor>singletonList(new ApiV2RequestInterceptor()));
    }

    private static String sha1Hash(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        byte[] hash = digest.digest(str.getBytes("UTF-8"));
        return new String(Hex.encode(hash));
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }

    public void setGcmRegistered(boolean gcmRegistered) {
        this.gcmRegistered = gcmRegistered;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public void invalidate() {
        authenticated = false;
        clientSessionId = null;
        securitySessionId = null;
        canReLog = false;
        getCookieStore().clear();
    }

    public String getBase() {
        if (useLocal) {
            return localUrl;
        }
        return remoteUrl;
    }

    private synchronized void nop() {
        try {
            UserSessionRest resp = getForObject("v2/user/nop", UserSessionRest.class);
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }

    /**
     * @param url
     * @param method
     * @param requestCallback
     * @param responseExtractor
     * @param urlVariables
     * @param <T>
     * @return
     * @throws RestClientException
     * @throws ResourceAccessException
     * @throws ResourceNotFoundException
     * @throws RestObjectClientException
     */
    @Override
    public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor,
                         Map<String, ?> urlVariables) {
        Log.d(TAG, "request url: " + getBase() + url + " method: " + method.name());
        URI expanded = new UriTemplate(getBase() + url).expand(urlVariables);
        return handleExecution(expanded, method, requestCallback, responseExtractor);
    }

    /**
     * @param url
     * @param method
     * @param requestCallback
     * @param responseExtractor
     * @param urlVariables
     * @param <T>
     * @return
     * @throws RestClientException
     * @throws ResourceAccessException
     * @throws ResourceNotFoundException
     * @throws RestObjectClientException
     */
    @Override
    public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Object... urlVariables) {
        Log.d(TAG, "request url: " + getBase() + url + " method: " + method.name());
        URI expanded = new UriTemplate(getBase() + url).expand(urlVariables);
        return handleExecution(expanded, method, requestCallback, responseExtractor);
    }


    public <T> T handleExecution(URI expanded, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) {
        if (isOK()) {
            if (authenticated) {
                try {
                    return super.execute(expanded, method, requestCallback, responseExtractor);
                } catch (HttpStatusCodeException e) {
                    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        reLoginInternal();
                        return doExecute(expanded, method, requestCallback, responseExtractor);
                    }
                    throw e;
                }
            } else {
                if (!canReLog)
                    throw new LoginFailedException("Re-login not allowed");
                reLoginInternal();
            }
        }
        return doExecute(expanded, method, requestCallback, responseExtractor);
    }


    private boolean isOK() {
        return !isLoginFlag && !isLogoutFlag && !isPassRecFlag && !isRegFLag;
    }

    @Override
    public <T> T execute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) {
        throw new UnsupportedOperationException();
    }

    public boolean isUseLocal() {
        return useLocal;
    }

    public void setUseLocal(boolean useLocal) {
        this.useLocal = useLocal;
    }

    public String pinLogin(String pin) {
        UserSessionRest init;
        init = getForObject("v2/security/session/init/", UserSessionRest.class);
        if (!init.response.success) {
            Log.d(TAG, "isInit ?" + init.success);
            Log.d(TAG, "errorMessage: " + init.error);
            return init.error;
        }
        clientSessionId = init.jsessionid;
        Log.d(TAG, "secureSessionId? " + init.response.secureSessionId);
        Log.d(TAG, "success? " + init.response.success);
        Log.d(TAG, "nonce? " + init.response.nonce);
        Log.d(TAG, "salt? " + init.response.salt);
        Log.d(TAG, "pin? " + pin);
        String calculatedPin = null;
        try {
            calculatedPin = sha1Hash(init.response.nonce + sha1Hash(init.response.salt + pin));
        } catch (Exception e) {
            return e.getMessage();
        }
        Log.d(TAG, "calculatedPin: " + calculatedPin);
        UserSessionRest resp = getForObject("v2/security/session/login/{secureSessionID}?token={calculatedPin}", UserSessionRest.class,
                init.response.secureSessionId, calculatedPin);
        if (resp.response.success) {
            Log.d(TAG, "isResp? " + resp.success + " secureClientSessionID: " + resp.response.secureSessionId);
            setSecuritySessionId(resp.response);
            return null;

        }
        Log.d(TAG, "isResp? " + resp.success + " errorMessage: " + resp.error);
        securitySessionId = null;
        return resp.error;
    }

    public boolean synchronize() throws Exception {
        try {
            Log.d(TAG, "Starting synchronization...");
            RestObject resp = getForObject("v2/box/synchronize?ifNeeded=false&wait=true&timeout=30", RestObject.class);
            Log.d(TAG, "Synchronization done and isSuccess? " + resp.isSuccess());
            return resp.isSuccess();
        } catch (Exception e) {
            Log.d(TAG, "Synchronization fail...", e);
            throw e;
        }
    }

    public void reset() {
        securitySessionId = null;
    }

    public boolean keepAlive() {
        Log.d(TAG, "keeping ssClientID: " + securitySessionId + " alive");
        UserSessionRest res = getForObject("v2/security/session/keepalive/{securitySessionId}", UserSessionRest.class, securitySessionId);
        return res.response.success;
    }

    public void unRegisterGCM(String gcmToken) {
        delete("v2/push-notification/gcm/token/{token}", gcmToken);
        gcmRegistered = false;
        if (callback != null)
            callback.onGCMUnregistered();
    }

    public void registerGCM(String token) {
        getForObject("v2/push-notification/gcm/register/{p_token}", HashMap.class, token);
        gcmToken = token;
        gcmRegistered = true;
        if (callback != null)
            callback.onGCMRegistered();
    }

    private void registerGCM() {
        registerGCM(gcmToken);
    }


    /**
     * @param username
     * @param password
     * @param serial
     * @return
     * @throws RestObjectException
     */
    public synchronized String login(String username, String password, String serial) {
        try {
            isLoginFlag = true;
            UserSessionRest resp;
            UserSessionRest init;
            if (authenticated) {
                init = logoutInternal();
            } else {
                init = getForObject("v2/user/init", UserSessionRest.class);
            }
            if (!init.success) {
                return (init.error == null) ? "" : init.error;
            }
            String calculatedPassword;
            try {
                calculatedPassword = sha1Hash(init.nonce + sha1Hash(password));
            } catch (Exception e) {
                return e.getMessage();
            }
            if (serial == null) {
                resp = getForObject("v2/user/login?username={username}&token={token}", UserSessionRest.class, username, calculatedPassword);
            } else {
                resp = getForObject("v2/user/login?username={username}&token={token}&serial={serial}", UserSessionRest.class, username, calculatedPassword,
                        serial);
            }
            //         }
            if (resp.success) {
                authenticated = true;
                canReLog = true;
                this.username = username;
                this.password = password;
                this.serial = serial;
                return null;
            }
            invalidate();
            return (resp.error == null) ? "" : resp.error;
        } finally {
            isLoginFlag = false;
        }
    }

    public boolean changePassword(String oldPassword, String newPassword) {

        try {
            UserSessionRest init = getForObject("v2/user/init", UserSessionRest.class);

            if (!init.success) {
                return false;
            }
            String calculatedPassword = null;
            try {
                calculatedPassword = sha1Hash(init.nonce + sha1Hash(oldPassword));
            } catch (Exception e) {
                return false;
            }

            UserSessionRest resp = postForObject("/v2/user/changePassword?token={token}", newPassword, UserSessionRest.class, calculatedPassword);

            //
            if (resp.success) {
                authenticated = true;
                canReLog = true;
                password = newPassword;
                return true;
            }
            // invalidate();
            return false;
        } finally {
            //empty
        }
    }

    private void reLoginInternal() {
        synchronized (lock) {
            if (authenticated) //Darko do not fucking delete that again or i killlllll you and i send your body to cameroon!!! so no one will find it grrrrrrrr
                return;
            if (!hasCredentials()) {
                if (callback != null) {
                    callback.loginFailed(null);
                }
                throw new LoginFailedException("no credentials");

            }

            String error = login(username, password, serial);

            if (error != null) {
                if (callback != null) {
                    callback.loginFailed(error);
                }
                throw new LoginFailedException("re-login failed");
            }

            try {
                if (!gcmRegistered && !useLocal && !gcmToken.isEmpty()) {
                    registerGCM();
                } else Log.d(TAG, "Already registered gcm skipping...");
            } catch (Exception e) {

                Log.e(TAG, "", e);

                try {
                    logoutInternal();
                } catch (Exception g) {
                    Log.e(TAG, "", g);
                }

                if (callback != null)
                    callback.loginFailed(e.getMessage());

                throw e;

            }

        }
    }

    private boolean hasCredentials() {
        if (!canReLog)
            return false;
        return (username != null) && (password != null) && !username.isEmpty() && !password.isEmpty();
    }

    public void relogin() {
        if (!authenticated) {
            reLoginInternal();
        }
    }

    public String logout() {
        synchronized (lock) {
            Log.e(TAG, "Login out.....");
            UserSessionRest resp = logoutInternal();
            return resp.error;
        }
    }

    private UserSessionRest logoutInternal() {
        try {
            synchronized (lock) {
                isLogoutFlag = true;
                UserSessionRest resp;
                Log.e(TAG, "sending request");
                resp = getForObject("v2/user/logout", UserSessionRest.class);
                if (gcmRegistered) {
                    unRegisterGCM(gcmToken);
                } else Log.d(TAG, "GCM is not registered skipping un-registration at logout");

                return resp;
            }
        } finally {
            invalidate();
            isLogoutFlag = false;
        }
    }

    public RestObject register(User user) {
        try {
            isRegFLag = true;
            RestObject obj;
            try {
                obj = postForObject("v2/user/register", user, UserSessionRest.class);
            } catch (RestObjectException e) {
                obj = e.getResponseBody();
            }
            return obj;
        } finally {
            isRegFLag = false;
        }
    }

    public RestObject recovery(String email) {
        try {
            RestObject obj;
            isPassRecFlag = true;
            try {
                obj = getForObject("v2/user/restore/?username=" + email, UserSessionRest.class);
            } catch (RestObjectException e) {
                obj = e.getResponseBody();
            }
            return obj;
        } finally {
            isPassRecFlag = false;
        }
    }

    public boolean verify(String username, String token) {
        try {
            UserSessionRest resp = getForObject("v2/user/verify?username={username}&token={token}", UserSessionRest.class, username,
                    token);
            return resp.success;
        } finally {
            // Empty
        }
    }

    public boolean restore(String username) {
        try {
            UserSessionRest resp = getForObject("v2/user/restore?username={username}", UserSessionRest.class, username);
            return resp.success;
        } finally {
            // Empty
        }
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getSecuritySessionId() {
        return securitySessionId;
    }

    private void setSecuritySessionId(SecurityUpdateItem securityUpdateItem) {
        if (securityUpdateItem != null) {
            securitySessionId = securityUpdateItem.secureSessionId;
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void clearSecureSessionId() {
        securitySessionId = null;
    }

    public APIV2RestCallback getCallback() {
        return callback;
    }

    public void setCallback(APIV2RestCallback callback) {
        this.callback = callback;
    }

    public String getClientSessionId() {
        return clientSessionId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User getUser() {
        return getForObject("v2/users/current", User.class);
    }


    public class ApiV2RequestInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution e) throws IOException {
            final HttpHeaders headers = httpRequest.getHeaders();
            headers.add("User-Agent", "Zipato-Android-App/2.0");
            if (local != null)
                headers.add("Accept-Language", local);
            ClientHttpResponse response = e.execute(httpRequest, bytes);

            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                authenticated = false;
            }
            return response;
        }
    }
}