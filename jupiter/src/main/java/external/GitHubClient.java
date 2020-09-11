package external;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import internalpurify.Item;
import internalpurify.Item.ItemBuilder;
// get responds from GitHUb JOb
public class GitHubClient {
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	private static final String DEFAULT_KEYWORD = "developer";

	public List<Item> search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = String.format(URL_TEMPLATE, keyword, lat, lon);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();

	    // Create a custom response handler
	    ResponseHandler<List<Item>> responseHandler = new ResponseHandler<List<Item>>() {

	        @Override
	        public List<Item> handleResponse(
	                final HttpResponse response) throws IOException {
	            if (response.getStatusLine().getStatusCode() != 200) {
	            	return new ArrayList<>();
	            }
	            HttpEntity entity = response.getEntity();
	            if (entity == null) {
	            	return new ArrayList<>();
	            }
	            
	            String responseBody = EntityUtils.toString(entity);
	            JSONArray array = new JSONArray(responseBody);
	            return getItemList(array); //purify raw data from GitHub Job Search responds

	        }
	    };
	    
		try {
			return httpclient.execute( new HttpGet(url), responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new ArrayList<>();

	}
	
	//purify raw data from GitHub Job Search responds
	
	private List<Item> getItemList(JSONArray array) {
		
		List<Item> itemList = new ArrayList<>();
		
		List<String> descriptionList = new ArrayList<>();
		
		for (int i = 0; i < array.length(); i++) {
			//need to extract keywords from description since GitHub API doesn't return keywords.
			String description = getStringFieldOrEmpty(array.getJSONObject(i), "description");
			if (description.equals("") || description.equals("\n")) {
				descriptionList.add(getStringFieldOrEmpty(array.getJSONObject(i), "title"));
			} else {
				descriptionList.add(description);
			}	
		}

		// We need to get keywords from multiple text in one request since
		// MonkeyLearnAPI has limitations on request per minute.
		
		String[] descriptionArray = descriptionList.toArray(new String[descriptionList.size()]);
		List<List<String>> keywords = MonkeyLearnClient.extractKeywords(descriptionArray);
		// String Array --> Keywords arrays
		
		
		
		for( int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			// object --> item
			//use ItemBuilder 
			ItemBuilder builder = new ItemBuilder();
			//
			builder.setItemId(getStringFieldOrEmpty(object, "id"));
			builder.setName(getStringFieldOrEmpty(object, "title"));
			builder.setAddress(getStringFieldOrEmpty(object, "location"));
			builder.setUrl(getStringFieldOrEmpty(object, "url"));
			builder.setImageUrl(getStringFieldOrEmpty(object, "company_logo"));
			builder.setKeywords(new HashSet<String>(keywords.get(i)));
			//读了每一个keywords array, 把他们convert成 HashSet;
			Item item = builder.build();
			itemList.add(item);

			
		}
		
		return itemList;
		
	}
	// determine if the field in the JasonObject is Null, if it is null return "", otherwise return obj.getString;

	private String getStringFieldOrEmpty(JSONObject obj, String field) {
		return obj.isNull(field) ? "" : obj.getString(field);
		
	}
	
}
