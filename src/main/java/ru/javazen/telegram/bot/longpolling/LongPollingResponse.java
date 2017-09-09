package ru.javazen.telegram.bot.longpolling;

import ru.javazen.telegram.bot.entity.Update;

import java.util.List;

public class LongPollingResponse {

    private Boolean ok;
    private String description;
    private List<Update> result;
    private Integer errorCode;

    public Boolean getOk() {
        return ok;
    }

    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Update> getResult() {
        return result;
    }

    public void setResult(List<Update> result) {
        this.result = result;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
}
