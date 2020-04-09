package minek.ckan.v3.datastore.service;

import minek.ckan.v3.datastore.model.*;
import minek.ckan.v3.datastore.service.command.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import java.util.UUID;

// https://docs.ckan.org/en/2.8/maintaining/datastore.html#ckanext.datastore.logic.action.datastore_search
// https://github.com/ckan/ckan/blob/master/ckanext/datastore/logic/action.py
public interface DatastoreService {

    @POST("api/3/action/datastore_create")
    Call<DatastoreCreateResult> datastoreCreate(@Body DatastoreCreate datastoreCreate);

    @FormUrlEncoded
    @POST("api/3/action/datastore_run_triggers")
    Call<Integer> datastoreRunTriggers(@Field("resource_id") UUID id);

    @POST("api/3/action/datastore_upsert")
    Call<DatastoreUpsertResult> datastoreUpsert(@Body DatastoreUpsert datastoreUpsert);

    @FormUrlEncoded
    @POST("api/3/action/datastore_info")
    Call<DatastoreInfo> datastoreInfo(@Field("id") UUID id);

    @POST("api/3/action/datastore_delete")
    Call<DatastoreDeleteResult> datastoreDelete(@Body DatastoreDelete datastoreDelete);

    // datastore_search

    @FormUrlEncoded
    @POST("api/3/action/datastore_search_sql")
    Call<DatastoreSearchSqlResult> datastoreSearchSql(@Field("sql") String sql);

    default Call<DatastoreSearchSqlResult> datastoreSearchSql(DatastoreSearchSql datastoreSearchSql) {
        return datastoreSearchSql(datastoreSearchSql.query());
    }

//    @POST("api/3/action/set_datastore_active_flag")
//    Call<Void> setDatastoreActiveFlag();

    @POST("api/3/action/datastore_function_create")
    Call<Void> datastoreFunctionCreate(@Body DatastoreFunctionCreate datastoreFunctionCreate);

    @FormUrlEncoded
    @POST("api/3/action/datastore_function_delete")
    Call<Void> datastoreFunctionDelete(@Field("name") String name);

    // get_all_resources_ids_in_datastore

}