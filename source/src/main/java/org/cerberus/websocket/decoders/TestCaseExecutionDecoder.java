package org.cerberus.websocket.decoders;

import com.google.gson.Gson;
import org.cerberus.crud.entity.TestCaseExecution;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

/**
 * Created by corentin on 31/10/16.
 */
public class TestCaseExecutionDecoder implements Decoder.Text<TestCaseExecution> {

    @Override
    public TestCaseExecution decode(String s) throws DecodeException {
        Gson gson = new Gson();
        return gson.fromJson(s,TestCaseExecution.class);
    }

    @Override
    public boolean willDecode(String s) {
        return false;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
