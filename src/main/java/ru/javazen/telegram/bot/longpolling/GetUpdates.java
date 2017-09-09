package ru.javazen.telegram.bot.longpolling;

import ru.javazen.telegram.bot.method.ApiMethod;

import java.util.List;

public class GetUpdates implements ApiMethod {
    private static final String METHOD = "getUpdates";

    private Integer offset;
    private Integer limit;
    private Integer timeout;
    private List<String> allowedUpdates;

    @Override
    public String getMethod() {
        return METHOD;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public List<String> getAllowedUpdates() {
        return allowedUpdates;
    }

    public void setAllowedUpdates(List<String> allowedUpdates) {
        this.allowedUpdates = allowedUpdates;
    }
}
