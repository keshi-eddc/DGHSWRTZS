package com.eddc.jnj.model;

/**
 * @Description: 强生项目账号管理 模型
 * @Author: keshi
 * @CreateDate: 2018年11月30日 11:46
 * @UpdateUser:
 * @UpdateDate:
 * @UpdateRemark:
 * @Version: 1.0
 */

public class SycmAccount {

    private Long id;

    private String name;

    private String password;

    private String token;

    private String sellerCookie;

    private String sycmCookie;

    private String sycmGroup;

    private String userid;

    private String shopName;

    private String loginStatus;

    private String status;

    private String cookieStatus;

    private String chituCookie;

    private String shopUrl;

    private String server;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getSellerCookie() {
        return sellerCookie;
    }

    public void setSellerCookie(String sellerCookie) {
        this.sellerCookie = sellerCookie == null ? null : sellerCookie.trim();
    }

    public String getSycmCookie() {
        return sycmCookie;
    }

    public void setSycmCookie(String sycmCookie) {
        this.sycmCookie = sycmCookie == null ? null : sycmCookie.trim();
    }

    public String getSycmGroup() {
        return sycmGroup;
    }

    public void setSycmGroup(String sycmGroup) {
        this.sycmGroup = sycmGroup == null ? null : sycmGroup.trim();
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid == null ? null : userid.trim();
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName == null ? null : shopName.trim();
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus == null ? null : loginStatus.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getCookieStatus() {
        return cookieStatus;
    }

    public void setCookieStatus(String cookieStatus) {
        this.cookieStatus = cookieStatus == null ? null : cookieStatus.trim();
    }

    public String getChituCookie() {
        return chituCookie;
    }

    public void setChituCookie(String chituCookie) {
        this.chituCookie = chituCookie == null ? null : chituCookie.trim();
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl == null ? null : shopUrl.trim();
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server == null ? null : server.trim();
    }

    @Override
    public String toString() {
        String show = "[ id:" + id +
                " ,name:" + name +
                " ,password:" + password +
                " ,token:" + token +
                " ,sellerCookie:" + sellerCookie +
                " ,sycmCookie:" + sycmCookie +
                " ,sycmGroup:" + sycmGroup +
                " ,userid:" + userid +
                " ,shopName:" + shopName +
                " ,loginStatus:" + loginStatus +
                " ,status:" + status +
                " ,cookieStatus:" + cookieStatus +
                " ,chituCookie:" + chituCookie +
                " ,shopUrl:" + shopUrl +
                " ,server:" + server +
                " ]";
        return show;
    }
}