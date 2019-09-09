package com.example.android_camera_client;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectResult;

public class UploadS3 extends AsyncTask<UploadParams, Void, String> {

    public String TAG = "UploadS3";

    FileCredentials credentials;
    UploadParams params;

    public UploadS3(FileCredentials _credentials) {
        super();
        credentials = _credentials;
    }

    @Override
    protected String doInBackground(UploadParams... _params) {
        if (credentials == null) {
            Log.d(TAG, "Cannot upload to S3. Credentials not found.");
            return null;
        }
        params = _params[0];
        File file = new File(params.pathToFile);
        String key = file.getName();
        Log.d(TAG, key);
        AmazonS3Client client = new AmazonS3Client(credentials.awsCredentials);
        client.setRegion(Region.getRegion(credentials.region));
        PutObjectResult result = client.putObject(credentials.bucketName, key, file);
        AccessControlList acl = client.getObjectAcl(credentials.bucketName, key);
        acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
        client.setObjectAcl(credentials.bucketName, key, acl);
        return key;
    }

    @Override
    protected void onPostExecute(String key) {
        params.listener.sendSuccess(params.captureId, key);

    }
}
