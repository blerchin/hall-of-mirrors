package com.example.android_camera_client;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.net.URI;

import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectResult;

class UploadParams {
    UploadParams(URI path, int id, CaptureListener cl) {
        pathToFile = path;
        captureId = id;
        listener = cl;
    }
    public URI pathToFile;
    public int captureId;
    public final CaptureListener listener;
}

public class UploadS3 extends AsyncTask<UploadParams, Void, String> {

    public String TAG = "UploadS3";

    S3Credentials credentials;
    UploadParams params;

    public UploadS3() {
        credentials = new S3Credentials();
        credentials.getCredentials();
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
