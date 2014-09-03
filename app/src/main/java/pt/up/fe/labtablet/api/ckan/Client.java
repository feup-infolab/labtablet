package pt.up.fe.labtablet.api.ckan;


import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * The primary interface to this package the Client class is responsible
 * for managing all interactions with a given connection.
 *
 * @author      Ross Jones <ross.jones@okfn.org>
 * @version     1.7
 * @since       2012-05-01
 */
public final class Client {

    private Connection _connection = null;

    /**
    * Constructs a new Client for making requests to a remote CKAN instance.
    *
    * @param  c A Connection object containing info on the location of the
    *         CKAN Instance.
    * @param  apikey A user's API Key sent with every request.
    */
    public Client( Connection c, String apikey ) {
        this._connection = c;
        this._connection.setApiKey(apikey);
    }

    /**
    * Loads a JSON string into a class of the specified type.
    */
    protected <T> T LoadClass( Class<T> cls, String data ) {
        Gson gson = new Gson();
        return gson.fromJson(data, cls);
    }

    /**
    * Handles error responses from CKAN
    *
    * When given a JSON string it will generate a valid CKANException
    * containing all of the error messages from the JSON.
    *
    * @param  json The JSON response
    * @param  action The name of the action calling this for the primary
    *         error message.
    * @throws CKANException containing the error messages contained in the
    *         provided JSON.
    */
    private void HandleError( String json, String action )
          throws CKANException {

        CKANException exception = new CKANException("Errors occured performing: " + action);

        HashMap hm  = LoadClass( HashMap.class, json);
        Map<String,Object> m = (Map<String,Object>)hm.get("error");
        for (Map.Entry<String,Object> entry : m.entrySet()) {
            if ( entry.getKey().startsWith("_") )
                continue;
            exception.addError( entry.getValue() + " - " + entry.getKey() );
        }
        throw exception;
    }


    /**
    * Retrieves a dataset
    *
    * Retrieves the dataset with the given name, or ID, from the CKAN
    * connection specified in the Client constructor.
    *
    * @param  name The name or ID of the dataset to fetch
    * @returns The Dataset for the provided name.
    * @throws CKANException if the request fails
    */
    public Dataset getDataset(String name)
            throws CKANException {
        String returned_json = this._connection.Post("/api/action/package_show",
                                                     "{\"id\":\"" + name + "\"}" );
        Dataset.Response r = LoadClass( Dataset.Response.class, returned_json);
        if ( ! r.success ) {
            HandleError( returned_json, "getDataset");
        }
        return r.result;
    }

    /**
    * Deletes a dataset
    *
    * Deletes the dataset specified with the provided name/id
    *
    * @param  name The name or ID of the dataset to delete
    * @throws CKANException if the request fails
    */
    public void deleteDataset(String name)
            throws CKANException {
        String returned_json = this._connection.Post("/api/action/package_delete",
                                                     "{\"id\":\"" + name + "\"}" );
        Dataset.Response r = LoadClass( Dataset.Response.class, returned_json);
        if ( ! r.success ) {
            HandleError( returned_json, "deleteDataset");
        }
    }

    /**
    * Creates a dataset on the server
    *
    * Takes the provided dataset and sends it to the server to
    * perform an create, and then returns the newly created dataset.
    *
    * @param  dataset A dataset instance
    * @returns The Dataset as it now exists
    * @throws CKANException if the request fails
    */
    public Dataset createDataset(Dataset dataset)
            throws CKANException {
        Gson gson = new Gson();
        String data = gson.toJson( dataset );
        System.out.println( data );
        String returned_json = this._connection.Post("/api/action/package_create",
                                                     data );
        System.out.println( returned_json );
        Dataset.Response r = LoadClass( Dataset.Response.class, returned_json);
        if ( ! r.success ) {
            // This will always throw an exception
            HandleError(returned_json, "createDataset");
        }
        return r.result;
    }


    /**
    * Retrieves a group
    *
    * Retrieves the group with the given name, or ID, from the CKAN
    * connection specified in the Client constructor.
    *
    * @param  name The name or ID of the group to fetch
    * @returns The Group instance for the provided name.
    * @throws A CKANException if the request fails
    */
    public Group getGroup(String name)
            throws CKANException {
        String returned_json = this._connection.Post("/api/action/group_show",
                                                     "{\"id\":\"" + name + "\"}" );
        Group.Response r = LoadClass( Group.Response.class, returned_json);
        if ( ! r.success ) {
            HandleError(returned_json, "getGroup");
        }
        return r.result;
    }

  /**
    * Deletes a Group
    *
    * Deletes the group specified with the provided name/id
    *
    * @param  name The name or ID of the group to delete
    * @throws A CKANException if the request fails
    */
    public void deleteGroup(String name)
            throws CKANException {
        String returned_json = this._connection.Post("/api/action/group_delete",
                                                     "{\"id\":\"" + name + "\"}" );
        Group.Response r = LoadClass( Group.Response.class, returned_json);
        if ( ! r.success ) {
            HandleError( returned_json, "deleteGroup");
        }
    }

    /**
    * Creates a Group on the server
    *
    * Takes the provided Group and sends it to the server to
    * perform an create, and then returns the newly created Group.
    *
    * @param  group A Group instance
    * @returns The Group as it now exists on the server
    * @throws CKANException if the request fails
    */
    public Group createGroup(Group group)
            throws CKANException {
        Gson gson = new Gson();
        String data = gson.toJson( group );
        String returned_json = this._connection.Post("/api/action/package_create",
                                                     data );
        Group.Response r = LoadClass( Group.Response.class, returned_json);
        if ( ! r.success ) {
            // This will always throw an exception
            HandleError(returned_json, "createGroup");
        }
        return r.result;
    }


    /**
    * Uses the provided search term to find datasets on the server
    *
    * Takes the provided query and locates those datasets that match the query
    *
    * @param  query The search terms
    * @returns A SearchResults object that contains a count and the objects
    * @throws CKANException if the request fails
    */
    public Dataset.SearchResults findDatasets(String query)
            throws CKANException {

        String returned_json = this._connection.Post("/api/action/package_search",
                                                     "{\"q\":\"" + query +"\"}" );
        Dataset.SearchResponse sr = LoadClass( Dataset.SearchResponse.class, returned_json);
        if ( ! sr.success ) {
            // This will always throw an exception
            HandleError(returned_json, "findDatasets");
        }
        return sr.result;
    }

}






