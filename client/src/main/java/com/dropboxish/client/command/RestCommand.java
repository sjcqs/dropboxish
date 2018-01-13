package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.RequestManager;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/30/17.
 * A {@link Command} that uses the REST/API
 */
abstract class RestCommand extends Command {
    private final static Logger logger = Logger.getLogger("Command");
    /**
     * The path for the request
     */
    private final String path;
    /**
     * HTTP Method for the Rest request
     */
    private final String method;

    protected RestCommand(String name, User user, String path, String method) {
        super(name, user);
        this.path = path;
        this.method = method;
    }

    protected RestCommand(String name, User user, String path, String method, boolean thread) {
        super(name, user, thread);
        this.path = path;
        this.method = method;
    }

    protected String sendRequest(){
        return sendRequest(null);
    }

    protected String sendRequest(Map<String, String> params)
            throws CommandIllegalArgumentException{
        RequestManager manager = getUser().getRequestManager();
        Response response = manager.sendRequest(path, params, method);
        Response.StatusType status = response.getStatusInfo();

        checkStatus(status, manager, response);
        return response.readEntity(String.class);
    }

    private void checkStatus(Response.StatusType status, RequestManager manager, Response response)
            throws CommandIllegalArgumentException{
        if (!status.getFamily().equals(Response.Status.Family.SUCCESSFUL)){
            if (status.getStatusCode() == 401){
                logger.info("Disconnected");
                manager.disconnect();
            }
            throw new CommandIllegalArgumentException(response.readEntity(String.class));
        }
    }

    protected Response sendRequest2(Map<String, String> params)
            throws CommandIllegalArgumentException{
        RequestManager manager = getUser().getRequestManager();
        Response response = manager.sendRequest(path, params, method);
        Response.StatusType status = response.getStatusInfo();
        checkStatus(status, manager, response);
        return response;
    }
}
