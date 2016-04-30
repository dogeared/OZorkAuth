package com.stormpath.ozorkauth.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class CommandResponse {
    private String[] gameInfo;
    private String request;
    private String[] response;
    private String[] look;
    private String status;
    private String message;

    public String[] getGameInfo() {
        return gameInfo;
    }

    public void setGameInfo(String[] gameInfo) {
        this.gameInfo = gameInfo;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String[] getResponse() {
        return response;
    }

    public void setResponse(String[] response) {
        this.response = response;
    }

    public String[] getLook() {
        return look;
    }

    public void setLook(String[] look) {
        this.look = look;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
