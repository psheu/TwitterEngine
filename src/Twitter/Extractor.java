package Twitter;
import java.util.*;
import java.util.regex.*;

// extracting usernames, hashtags, URLs from Tweet

public class Extractor{
	  public static class Entity {
		    public enum Type {
		      URL, HASHTAG, MENTION
		    }
		    protected int start;
		    protected int end;
		    protected final String value;
		    protected final String listSlug; // listSlug stores the list of @mention/list
		    protected final Type type;

		    protected String displayURL = null;
		    protected String expandedURL = null;

		    public Entity(int start, int end, String value, String listSlug, Type type) {
		      this.start = start;
		      this.end = end;
		      this.value = value;
		      this.listSlug = listSlug;
		      this.type = type;
		    }

		    public Entity(int start, int end, String value, Type type) {
		      this(start, end, value, null, type);
		    }

		    public Entity(Matcher matcher, Type type, int groupNumber) {
		      // Offset -1 on start index to include @, # symbols for mentions and hashtags
		      this(matcher, type, groupNumber, -1);
		    }

		    public Entity(Matcher matcher, Type type, int groupNumber, int startOffset) {
		      this(matcher.start(groupNumber) + startOffset, matcher.end(groupNumber), matcher.group(groupNumber), type);
		    }

		    @Override
		    public boolean equals(Object obj) {
		      if (this == obj) {
		        return true;
		      }

		      if (!(obj instanceof Entity)) {
		        return false;
		      }

		      Entity other = (Entity)obj;

		      if (this.type.equals(other.type) &&
		          this.start == other.start &&
		          this.end == other.end &&
		          this.value.equals(other.value)) {
		        return true;
		      } else {
		        return false;
		      }
		    }

		    @Override
		    public int hashCode() {
		      return this.type.hashCode() + this.value.hashCode() + this.start + this.end;
		    }

		    @Override
		    public String toString() {
		      return value + "(" + type +") [" +start + "," + end +"]";
		    }

		    public Integer getStart() {
		      return start;
		    }

		    public Integer getEnd() {
		      return end;
		    }

		    public String getValue() {
		      return value;
		    }

		    public String getListSlug() {
		      return listSlug;
		    }

		    public Type getType() {
		      return type;
		    }

		    public String getDisplayURL() {
		      return displayURL;
		    }

		    public void setDisplayURL(String displayURL) {
		      this.displayURL = displayURL;
		    }

		    public String getExpandedURL() {
		      return expandedURL;
		    }

		    public void setExpandedURL(String expandedURL) {
		      this.expandedURL = expandedURL;
		    }
		  }

		  protected boolean extractURLWithoutProtocol = true;

		  // new extractor
		  public Extractor() {
		  }

		  // Extract URLs, @mentions, lists and #hashtag from a given text/tweet.
		  // @param text text of tweet
		  // @return list of extracted entities
		  public List<Entity> extractEntitiesWithIndices(String text) {
		    List<Entity> entities = new ArrayList<Entity>();
		    entities.addAll(extractURLsWithIndices(text));
		    entities.addAll(extractHashtagsWithIndices(text, false));
		    entities.addAll(extractMentionsOrListsWithIndices(text));

		    return entities;
		  }

		  //Extract @username references from Tweet text (mention)
		  public List<String> extractMentionedScreennames(String text) {
		    if (text == null || text.length() == 0) {
		      return Collections.emptyList();
		    }

		    List<String> extracted = new ArrayList<String>();
		    for (Entity entity : extractMentionedScreennamesWithIndices(text)) {
		      extracted.add(entity.value);
		    }
		    return extracted;
		  }
		  public List<Entity> extractMentionedScreennamesWithIndices(String text) {
		    List<Entity> extracted = new ArrayList<Entity>();
		    for (Entity entity : extractMentionsOrListsWithIndices(text)) {
		      if (entity.listSlug == null) {
		        extracted.add(entity);
		      }
		    }
		    return extracted;
		  }

		  public List<Entity> extractMentionsOrListsWithIndices(String text) {
		    if (text == null || text.length() == 0) {
		      return Collections.emptyList();
		    }

		    // If text doesn't contain @/＠ at all, return an empty list
		    boolean found = false;
		    for (char c : text.toCharArray()) {
		      if (c == '@' || c == '＠') {
		        found = true;
		        break;
		      }
		    }
		    if (!found) {
		      return Collections.emptyList();
		    }

		    List<Entity> extracted = new ArrayList<Entity>();
		    Matcher matcher = Regex.VALID_MENTION_OR_LIST.matcher(text);
		    while (matcher.find()) {
		      String after = text.substring(matcher.end());
		      if (! Regex.INVALID_MENTION_MATCH_END.matcher(after).find()) {
		        if (matcher.group(Regex.VALID_MENTION_OR_LIST_GROUP_LIST) == null) {
		          extracted.add(new Entity(matcher, Entity.Type.MENTION, Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME));
		        } else {
		          extracted.add(new Entity(matcher.start(Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME) - 1,
		              matcher.end(Regex.VALID_MENTION_OR_LIST_GROUP_LIST),
		              matcher.group(Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME),
		              matcher.group(Regex.VALID_MENTION_OR_LIST_GROUP_LIST),
		              Entity.Type.MENTION));
		        }
		      }
		    }
		    return extracted;
		  }


		  // Extract URL references from Tweet
		  public List<String> extractURLs(String text) {
		    if (text == null || text.length() == 0) {
		      return Collections.emptyList();
		    }

		    List<String> urls = new ArrayList<String>();
		    for (Entity entity : extractURLsWithIndices(text)) {
		      urls.add(entity.value);
		    }
		    return urls;
		  }
		  public List<Entity> extractURLsWithIndices(String text) {
		    if (text == null || text.length() == 0
		        || (extractURLWithoutProtocol ? text.indexOf('.') : text.indexOf(':')) == -1) {
		      // If text doesn't contain '.' or ':' return an empty list (no URL in Tweet)
		      return Collections.emptyList();
		    }

		    List<Entity> urls = new ArrayList<Entity>();

		    Matcher matcher = Regex.VALID_URL.matcher(text);
		    while (matcher.find()) {
		      if (matcher.group(Regex.VALID_URL_GROUP_PROTOCOL) == null) {
		        // skip if protocol is not present and 'extractURLWithoutProtocol' is false
		        // or URL is preceded by invalid character.
		        if (!extractURLWithoutProtocol
		            || Regex.INVALID_URL_WITHOUT_PROTOCOL_MATCH_BEGIN
		                    .matcher(matcher.group(Regex.VALID_URL_GROUP_BEFORE)).matches()) {
		          continue;
		        }
		      }
		      String url = matcher.group(Regex.VALID_URL_GROUP_URL);
		      int start = matcher.start(Regex.VALID_URL_GROUP_URL);
		      int end = matcher.end(Regex.VALID_URL_GROUP_URL);
		      Matcher tco_matcher = Regex.VALID_TCO_URL.matcher(url);
		      if (tco_matcher.find()) {
		        // In the case of t.co URLs, don't allow additional path characters
		        url = tco_matcher.group();
		        end = start + url.length();
		      }

		      urls.add(new Entity(start, end, url, Entity.Type.URL));
		    }

		    return urls;
		  }


		  // Extract hashtag references from Tweet
		  public List<String> extractHashtags(String text) {
		    if (text == null || text.length() == 0) {
		      return Collections.emptyList();
		    }

		    List<String> extracted = new ArrayList<String>();
		    for (Entity entity : extractHashtagsWithIndices(text)) {
		      extracted.add(entity.value);
		    }

		    return extracted;
		  }

		  public List<Entity> extractHashtagsWithIndices(String text) {
		    return extractHashtagsWithIndices(text, true);
		  }
		  
		  private List<Entity> extractHashtagsWithIndices(String text, boolean checkUrlOverlap) {
		    if (text == null || text.length() == 0) {
		      return Collections.emptyList();
		    }

		    // If text doesn't contain #/＃ at all, simply return an empty list.
		    boolean found = false;
		    for (char c : text.toCharArray()) {
		      if (c == '#' || c == '＃') {
		        found = true;
		        break;
		      }
		    }
		    if (!found) {
		      return Collections.emptyList();
		    }

		    List<Entity> extracted = new ArrayList<Entity>();
		    Matcher matcher = Regex.VALID_HASHTAG.matcher(text);

		    while (matcher.find()) {
		      String after = text.substring(matcher.end());
		      if (!Regex.INVALID_HASHTAG_MATCH_END.matcher(after).find()) {
		        extracted.add(new Entity(matcher, Entity.Type.HASHTAG, Regex.VALID_HASHTAG_GROUP_TAG));
		      }
		    }

		    if (checkUrlOverlap) {
		      // extract URLs
		      List<Entity> urls = extractURLsWithIndices(text);
		      if (!urls.isEmpty()) {
		        extracted.addAll(urls);
		        // remove URL entities
		        Iterator<Entity> it = extracted.iterator();
		        while (it.hasNext()) {
		          Entity entity = it.next();
		          if (entity.getType() != Entity.Type.HASHTAG) {
		            it.remove();
		          }
		        }
		      }
		    }

		    return extracted;
		  }


		  public void setExtractURLWithoutProtocol(boolean extractURLWithoutProtocol) {
		    this.extractURLWithoutProtocol = extractURLWithoutProtocol;
		  }

		  public boolean isExtractURLWithoutProtocol() {
		    return extractURLWithoutProtocol;
		  }
}