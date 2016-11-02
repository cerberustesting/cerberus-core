package org.cerberus.websocket.encoders;

import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.service.ITestCaseStepExecutionService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * Created by corentin on 31/10/16.
 */
public class TestCaseExecutionEncoder  implements Encoder.Text<TestCaseExecution>  {

    @Autowired
    ITestCaseStepExecutionService testCaseStepExecutionService;

    @Override
    public String encode(TestCaseExecution testCaseExecution) throws EncodeException {
        return  testCaseExecution.toJson().toString();
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
