package com.jiang.duckojsandbox.service;


import com.jiang.duckojsandbox.model.ExecuteRequest;
import com.jiang.duckojsandbox.model.ExecuteResponse;

public interface CodeSandBox {

    /**
     * 执行代码：
     * @param executeRequest
     * @return
     */
    ExecuteResponse doExecute(ExecuteRequest executeRequest);
}
