package gr.atc.heads.io;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import gr.atc.common.utils.Utils;
import gr.atc.heads.model.HeadsPoint;
import gr.atc.heads.model.HeadsPointQuery;
import gr.atc.heads.model.Tag;
import gr.atc.heads.model.User;

public class HeadsClient implements IHeadsClient {

	private static String tag = "headsClient";
    private static String ACCESS_TOKEN_KEY = "AccessToken";

	private BackEndServicesURLProvider urlProvider;
	private DefaultHttpClient client;

	private String servicesUrl;

    private Context context;

    private String accessToken;

    private HeadsLoginListener headsLoginListener;

    private HeadsSearchListener HeadsSearchListener;

    private HeadsUploadListener HeadsUploadListener;

    private HeadsDeleteListener HeadsDeleteListener;

    private HeadsClientMessenger messenger;

    @Override
    public void setHeadsLoginListener(HeadsLoginListener HeadsLoginListener) {
        this.headsLoginListener = HeadsLoginListener;
    }

    @Override
    public void setHeadsSearchListener(HeadsSearchListener HeadsSearchListener) {
        this.HeadsSearchListener = HeadsSearchListener;
    }

    @Override
    public void setHeadsUploadListener(HeadsUploadListener HeadsUploadListener) {
        this.HeadsUploadListener = HeadsUploadListener;
    }

    @Override
    public void setHeadsDeleteListener(HeadsDeleteListener HeadsDeleteListener) {
        this.HeadsDeleteListener = HeadsDeleteListener;
    }

    @SuppressLint("DefaultLocale")
	private class BackEndServicesURLProvider {

		public String getLoginUrl() {
			return String.format("%s/Token", servicesUrl);
		}

		public String getTagsUrl() {
			return String.format("%s/model", servicesUrl);
		}

		public String getRegisterUrl() {
			return String.format("%s/api/Account/Register", servicesUrl);
		}

		public String getUploadUrl() {
			return String.format("%s/api/points", servicesUrl);
		}
		
		public String getSearchUrl() {
			return String.format("%s/api/points/search", servicesUrl);
		}

		public String getUserPackagesUrl() {
			return String.format("%s/api/points/search", servicesUrl);
		}
		
		public String getDeleteUserPackagesUrl(String packageId) {
			return String.format("%s/api/points/%s", servicesUrl, packageId);
		}

        public String getThumbnailUrl(String id) {
            return String.format("%s/Photos/Thumbnail/%s", servicesUrl, id);
        }

        public String getImageUrl(String id) {
            return String.format("%s/Photos/Full/%s", servicesUrl, id);
        }
	}

    @Override
    public String getThumbnailUrl(String id) {

        return urlProvider.getThumbnailUrl(id);
    }

    @Override
    public String getImageUrl(String id) {

        return urlProvider.getImageUrl(id);
    }

	HeadsClient(Context context, String servicesUrl) {

		this.servicesUrl = servicesUrl;
        this.context = context;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        accessToken = prefs.getString(ACCESS_TOKEN_KEY, "");

        messenger = new HeadsClientMessenger(handler);

		if (urlProvider == null) {
			urlProvider = new BackEndServicesURLProvider();
		}
		client = new DefaultHttpClient();
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				Utils.NETWORK_CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParameters,
				Utils.NETWORK_SOCKET_TIMEOUT);
		client.setParams(httpParameters);
	}

    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case HeadsClientMessenger.LOGIN_SUCCESSFUL:
                    if (headsLoginListener != null) {
                        User user = messenger.getUser(msg);
                        headsLoginListener.loginSuccessful(user);
                    }
                    break;
                case HeadsClientMessenger.REGISTER_SUCCESSFUL:
                    if (headsLoginListener != null) {
                        headsLoginListener.registerSuccessful();
                    }
                    break;
                case HeadsClientMessenger.TAGS_RECEIVED:
                    if (headsLoginListener != null) {
                        List<Tag> tags = messenger.getTags(msg);
                        headsLoginListener.tagsReceived(tags);
                    }
                    break;
                case HeadsClientMessenger.SEARCH_COMPLETED:
                    if (HeadsSearchListener != null) {
                        List<HeadsPoint> results = messenger.getResults(msg);
                        HeadsSearchListener.searchCompleted(results);
                    }
                    break;
                case HeadsClientMessenger.UPLOAD_COMPLETED:
                    if (HeadsUploadListener != null) {
                        String packageId = messenger.getPackageId(msg);
                        HeadsUploadListener.uploadCompleted(packageId);
                    }
                    break;
                case HeadsClientMessenger.DELETE_COMPLETED:
                    if (HeadsDeleteListener != null) {
                        HeadsDeleteListener.deleteSuccessful();
                    }
                    break;
                case HeadsClientMessenger.FAILURE:
                    String message = messenger.getFailureMessage(msg);
                    if (headsLoginListener != null) {
                        headsLoginListener.loginFailed(message);
                    }
                    else if (HeadsSearchListener != null) {
                        HeadsSearchListener.searchFailed(message);
                    }
                    else if (HeadsUploadListener != null) {
                        HeadsUploadListener.uploadFailed(message);
                    }
                    else if (HeadsDeleteListener != null) {
                        HeadsDeleteListener.deleteFailed(message);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void startRegister(final String username, final String password) {
        Thread t = new Thread() {
            public void run() {
                String response = register(username, password);

                if (response == null) {
                    messenger.sendFailureMessage("Register failed");
                    return;
                }
                else if (response.length() == 0) {
                    messenger.sendRegisterSuccessMessage();
                    return;
                }

                try {
                    JSONObject jsonResponse = new JSONObject(response);

                    if (jsonResponse.has("Message")) {
                        String errorMessage = jsonResponse.getString("Message");
                        JSONObject modelState = jsonResponse.getJSONObject("ModelState");
                        Iterator<String> keys = modelState.keys();
                        while(keys.hasNext()) {
                            String key = keys.next();
                            JSONArray descArray = modelState.getJSONArray(key);
                            if (descArray.length() > 0) {
                                errorMessage = descArray.getString(0);
                            }
                        }
                        messenger.sendFailureMessage(errorMessage);
                    }
                    else {

                    }
                }
                catch (JSONException e) {
                    messenger.sendFailureMessage(e.getMessage());
                }
            }
        };
        t.start();
    }

    @Override
    public void startLogin(final String username, final String password) {
        Thread t = new Thread() {
            public void run() {
                String response = login(username, password);

                if (response == null) {
                    messenger.sendFailureMessage("Login failed");
                    return;
                }

                try {
                    JSONObject loginJsonResponse = new JSONObject(response);

                    if (loginJsonResponse.has("error")) {
                        String errorMessage = loginJsonResponse.getString("error_description");
                        messenger.sendFailureMessage(errorMessage);
                    }
                    else {
                        accessToken = loginJsonResponse.getString("access_token");

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(ACCESS_TOKEN_KEY, accessToken);
                        editor.commit();
                        messenger.sendLoginSuccessMessage(username, username, "");
                    }
                }
                catch (JSONException e) {
                    messenger.sendFailureMessage(e.getMessage());
                }
            }
        };
        t.start();
    }

    // Setup a single response handler for all network operations to use
    private ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

        @Override
        public String handleResponse(HttpResponse response)
                throws IOException {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                Log.d(tag,
                        "Server returned STATUS " + String.valueOf(statusCode));
                Log.d(tag,
                        "Response " + EntityUtils.toString(response.getEntity(), HTTP.UTF_8));
                return "";
            }
            return EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
        }
    };

	private String login(String username, String password) {

		String httpUrl = urlProvider.getLoginUrl();

	    HttpPost httppost = new HttpPost(httpUrl);

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("username", username));
	        nameValuePairs.add(new BasicNameValuePair("password", password));
            nameValuePairs.add(new BasicNameValuePair("confirmPassword", password));
            nameValuePairs.add(new BasicNameValuePair("grant_type", "password"));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

	        HttpResponse httpResponse = client.execute(httppost);
	        String response = EntityUtils.toString(httpResponse.getEntity());
	        
	        Log.d(tag, "Http POST to " + httpUrl + " with params: "+ nameValuePairs);
	        Log.d(tag, " Response: " + response);

			return response;
	        
	    } catch (ClientProtocolException e) {
            Log.e(tag,"ClientProtocolException");
	    	return null;
	    } catch (IOException e) {
            Log.e(tag, "IOException");
	    	return null;
	    }
    }

    private String register(String username, String password) {

        String httpUrl = urlProvider.getRegisterUrl();

        HttpPost httppost = new HttpPost(httpUrl);

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username", username));
            nameValuePairs.add(new BasicNameValuePair("password", password));
            nameValuePairs.add(new BasicNameValuePair("confirmPassword", password));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

            HttpResponse httpResponse = client.execute(httppost);

            Log.d(tag, "Http POST to " + httpUrl + " with params: "+ nameValuePairs);

            String response = EntityUtils.toString(httpResponse.getEntity());
            Log.d(tag, " Response: " + response);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return  "";
            }

            return response;

        } catch (ClientProtocolException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void requestTags() {
        Thread t = new Thread() {
            public void run() {
                String url = urlProvider.getTagsUrl();
                List<Tag> tags = new ArrayList<Tag>();

                try {
                    HttpGet getMethod = getUtf8HttpGet(url);
                    Log.d(tag, "Http GET to " + url);
                    String responseBody = client.execute(getMethod, responseHandler);
                    if (responseBody == null || responseBody.length() == 0) {
                        messenger.sendFailureMessage("Communication error!");
                        return;
                    }

                    Log.d(tag, " Response: " + responseBody);

                    JSONObject response = new JSONObject(responseBody);

                    if (!response.getBoolean("status")) {
                        messenger.sendFailureMessage("Getting user failed");
                        return;
                    }

                    JSONArray result = response.getJSONArray("result");

                    for (int i = 0; i < result.length(); i++) {
                        JSONObject resultObject = result.getJSONObject(i);
                        //JSONObject object = resultObject.getJSONObject("object");

                        JSONArray vertices = resultObject.getJSONArray("vertices");

                        for (int j = 0; j < vertices.length(); j++) {
                            JSONObject obj = vertices.getJSONObject(j);

                            Tag tag = parseJSONTag(obj);
                            tags.add(tag);
                        }
                    }

                    messenger.sendTagsMessage(tags);
                } catch (Exception e) {
                    Utils.logNetworkError(e);
                    messenger.sendFailureMessage(e.getMessage());
                }
            }
        };
        t.start();
    }

	
	private Tag parseJSONTag(JSONObject object) throws Exception {
		long id = object.getLong("coreid");
		String name = object.getString("title");

        return new Tag(id, name);
	}

    @Override
	public void startUpload(final String userId, final HeadsPoint point) {
        Thread t = new Thread() {
            public void run() {

                String url = urlProvider.getUploadUrl();

                try {
                    JSONObject p = new JSONObject();
                    p.put("Title", point.getTitle());
                    p.put("Description", point.getDescription());
                    p.put("Image", point.getImage());
                    p.put("Latitude", point.getLatitude());
                    p.put("Longitude", point.getLongitude());

                    HttpPost postMethod = getUtf8HttpPost(url, p.toString());
                    postMethod.setHeader("Content-Type", "application/json");
                    postMethod.setHeader("Authorization", String.format("Bearer %s", accessToken));
                    Log.d(tag, "Http POST to " + url + " with body: " + p.toString());
                    String responseBody = client.execute(postMethod, responseHandler);
                    Log.d(tag, " Response: " + responseBody);

                    int newPointId = Integer.parseInt(responseBody);
                    messenger.sendUploadMessage(String.format("%d", newPointId));
                } catch (Exception e) {
                    Utils.logNetworkError(e);
                    messenger.sendFailureMessage(e.getMessage());
                }
            }
        };
        t.start();
	}

    @Override
	public void startSearch(final HeadsPointQuery query) {
        Thread t = new Thread() {
            public void run() {
                try {
                    Uri.Builder b = Uri.parse(urlProvider.getSearchUrl()).buildUpon();

                    if (query.getFrom()!=null) {
                        b.appendQueryParameter("from", query.getFrom());
                    }
                    if (query.getTo()!=null) {
                        b.appendQueryParameter("to", query.getTo());
                    }
                    if (query.getTitle()!=null) {
                        b.appendQueryParameter("title", query.getTitle());
                    }
                    if (query.getDescription()!=null) {
                        b.appendQueryParameter("description", query.getDescription());
                    }
                    b.appendQueryParameter("longitude",
                            String.format(Locale.US, "%f", query.getLongitude()));
                    b.appendQueryParameter("latitude",
                            String.format(Locale.US, "%f", query.getLatitude()));
                    b.appendQueryParameter("range",
                            String.format(Locale.US, "%f", query.getRange()));

                    String url = b.build().toString();
                    HttpGet getMethod = getUtf8HttpGet(url);

                    Log.d(tag, "Request: " + url);

                    String responseBody = client.execute(getMethod, responseHandler);
                    Log.d(tag, " Response: " + responseBody);

                    List<HeadsPoint> resultPackageList = getHeadsPackages(responseBody);

                    messenger.sendResultsMessage(resultPackageList);
                } catch (Exception e) {
                    Utils.logNetworkError(e);
                    messenger.sendFailureMessage(e.getMessage());
                }
            }
        };
        t.start();
	}

    private List<HeadsPoint> getHeadsPackages(String responseBody) throws JSONException {
        JSONArray response = new JSONArray(responseBody);

        List<HeadsPoint> resultPackageList = new ArrayList<HeadsPoint>();

        for (int i=0 ; i<response.length() ; i++) {
            if (response.isNull(i)) {
                continue;
            }

            JSONObject p = response.getJSONObject(i);
            String pointId = String.format("%d", p.getInt("PointId"));

            HeadsPoint point = new HeadsPoint();
            point.setId(pointId);
            point.setLatitude(p.getDouble("Latitude"));
            point.setLongitude(p.getDouble("Longitude"));
            point.setCaptureTime(p.getLong("Uploaded"));
            point.setDescription(p.getString("Description"));
            point.setTitle(p.getString("Title"));
            point.setUsername(p.getString("Username"));

            //Set the image URL
            point.setImageURL(getThumbnailUrl(pointId));
            point.setLargeImageUrl(getImageUrl(pointId));

            resultPackageList.add(point);
        }
        return resultPackageList;
    }

    @Override
    public void requestUserPackages(final String userId) {
        Thread t = new Thread() {
            public void run() {
                try {
                    Uri.Builder b = Uri.parse(urlProvider.getUserPackagesUrl()).buildUpon();
                    b.appendQueryParameter("username", userId);
                    String url = b.build().toString();
                    HttpGet getMethod = getUtf8HttpGet(url);

                    Log.d(tag, "Get request to: " + url);
                    String responseBody = client.execute(getMethod, responseHandler);
                    Log.d(tag, " Response: " + responseBody);

                    List<HeadsPoint> resultPackageList = getHeadsPackages(responseBody);

                    messenger.sendResultsMessage(resultPackageList);
                } catch (Exception e) {
                    Utils.logNetworkError(e);
                    messenger.sendFailureMessage(e.getMessage());
                }
            }
        };
        t.start();
	}

    @Override
	public void requestPackageDeletion(final String packageId) {
        Thread t = new Thread() {
            public void run() {
                String url = urlProvider.getDeleteUserPackagesUrl(packageId);

                try {
                    HttpDelete deleteMethod = getUtf8HttpDelete(url);
                    Log.d(tag, "Http Delete to " + url);
                    HttpResponse httpResponse = client.execute(deleteMethod);
                    int statusCode = httpResponse.getStatusLine().getStatusCode();

                    if (statusCode >=200 && statusCode <= 299) {
                        if (httpResponse.getEntity() != null) {
                            httpResponse.getEntity().consumeContent();
                        }
                        messenger.sendDeleteCompletedMessage();
                    }
                    else {
                        String response = EntityUtils.toString(httpResponse.getEntity());
                        Log.d(tag, " Response: " + response);
                        messenger.sendFailureMessage(response);
                    }
                } catch (Exception e) {
                    Utils.logNetworkError(e);
                    messenger.sendFailureMessage(e.getMessage());
                }
            }
        };
        t.start();
	}

	/**
	 * @param url
	 *            The url to post
	 * @param args
	 *            the data to post
	 * @return the post object that should be used by the HttpClient
	 * @throws java.io.UnsupportedEncodingException
	 */
	private static HttpPost getUtf8HttpPost(String url, String args)
			throws UnsupportedEncodingException {

		HttpPost postMethod = new HttpPost(url);

		postMethod.setHeader("Accept", "application/json");

		StringEntity postData = new StringEntity(args, "UTF-8");
		postData.setContentType("application/json; charset=UTF-8");
		postMethod.setEntity(postData);

		return postMethod;
	}

    private static HttpGet getUtf8HttpGet(String url)
			throws UnsupportedEncodingException {

		HttpGet getMethod = new HttpGet(url);

		getMethod.setHeader("Accept", "application/json");

		return getMethod;
	}

    private static HttpPut getUtf8HttpPut(String url, String args)
			throws UnsupportedEncodingException {

		HttpPut putMethod = new HttpPut(url);

		putMethod.setHeader("Accept", "application/json");

		StringEntity putData = new StringEntity(args, "UTF-8");
		putData.setContentType("application/json; charset=UTF-8");
		putMethod.setEntity(putData);

		return putMethod;
	}

    private static HttpDelete getUtf8HttpDelete(String url)
			throws UnsupportedEncodingException {

		HttpDelete deleteMethod = new HttpDelete(url);
		
//		deleteMethod.setHeader("Content-type", "application/json");
		deleteMethod.setHeader("Accept", "application/json");

		return deleteMethod;
	}

}
