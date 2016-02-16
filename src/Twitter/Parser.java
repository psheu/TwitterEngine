package Twitter;
import java.util.regex.*;
import java.util.regex.Pattern;
import java.util.ArrayList;

//extracts usernames, hashtags, URLs from Tweet

public class Parser {
	private String message;
	//Array that stores mentions in the tweet
	private ArrayList<String> mentions = new ArrayList<String>();
	//Array that stores hashtags in the tweet
	private ArrayList<String> hashtags = new ArrayList<String>();
	//Array that stores URLs in the tweet
	private ArrayList<String> urls = new ArrayList<String>();
	private boolean isValid = true;
		
	public Parser(String tweet){
		message = tweet;
		
		//check if tweet is valid
		if (tweet == null || tweet.length() == 0 || tweet.length()>= 140) {
			isValid = false;
		}
		
		//valid characters for mentions, hashtags, and URLs
		final String MENTION = "[@]+([A-Za-z0-9-_]+)";
		final String HASHTAG = "[#]+([A-Za-z0-9-_]+)";
		final String URL = "(https?)://[a-zA-Z_0-9\\-]+([a-zA-Z_0-9\\-.]*)+([#&-=?+%.]*)?";
		
		//find and extract the mentions, hashtags, and URLS
		Pattern mention = Pattern.compile(MENTION);
		Pattern hashtag = Pattern.compile(HASHTAG);
		Pattern url = Pattern.compile(URL);
		Matcher menMatcher = mention.matcher(message);
		Matcher hashMatcher = hashtag.matcher(message);
		Matcher urlMatcher = url.matcher(message);
		
		String temp;
		//add the mentions, hashtags, and URLs found to their respective arrays
		while (menMatcher.find()) {
			temp = menMatcher.group();
		    mentions.add(temp.substring(1));
		}
		while (hashMatcher.find()) {
			temp = hashMatcher.group();
			hashtags.add(temp.substring(1));
		}
		while (urlMatcher.find()) {
			urls.add(urlMatcher.group());
		}
	}
	
	//returns each array
	public ArrayList<String> getMentions(){
		return mentions;
	}
	public ArrayList<String> getHashtags(){
		return hashtags;
	}
	public ArrayList<String> getUrls(){
		return urls;
	}
	
	//returns the size of each array
	public int numMentions(){
		return mentions.size();
	}
	public int numHashtags(){
		return hashtags.size();
	}
	public int numUrls(){
		return urls.size();
	}
	
	//returns true if mention, hashtag, or URL is in Tweet
	public boolean hasMention(final String mention){
		return mentions.contains(mention);
	}
	public boolean hasHashtag(final String hashtag){
		return hashtags.contains(hashtag);
	}
	public boolean hasUrl(final String url){
		return urls.contains(url);
	}  
	
	//return if tweet is valid
	public boolean isValidTweet() {
		return isValid;
	}
}
		  
