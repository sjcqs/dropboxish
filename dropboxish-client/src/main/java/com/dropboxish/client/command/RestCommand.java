package com.dropboxish.client.command;

import com.dropboxish.client.Client;
import com.dropboxish.client.utils.RequestManager;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;

import java.util.Map;

/**
 * Created by satyan on 11/30/17.
 * A {@link Command} that uses the REST/API
 */
public abstract class RestCommand extends Command {
    /**
     * The path for the request
     */
    private final String path;
    /**
     * {@link HttpMethod} for the Rest request
     */
    private final HttpMethod method;
    private final boolean secured;

    protected RestCommand(String name, Client client, String path, HttpMethod method, boolean secured) {
        super(name, client);
        this.path = path;
        this.method = method;
        this.secured = secured;
    }

    protected ContentResponse sendRequest(){
        return sendRequest(null);
    }

    protected ContentResponse sendRequest(Map<String, String> params){
        RequestManager manager = getClient().getRequestManager();
        ContentResponse response = manager.sendRequest(path, params, method, secured);
        if (response.getStatus() != 200){
            throw new CommandIllegalArgumentException(response.getContentAsString());
        }
        return response;
    }
}
