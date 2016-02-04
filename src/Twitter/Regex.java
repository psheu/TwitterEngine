package Twitter;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

public class Regex {
	private static final String UNICODE_SPACES = "[" +
		    "\\u0020" +             // SPACE
		    "\\u0085" +             // NEXT LINE
		    "\\u2028" +             // LINE SEPARATOR
		    "\\u2029" +             // PARAGRAPH SEPARATOR
		  "]";

	private static final String HASHTAG_LETTERS = "\\p{L}\\p{M}";
	private static final String HASHTAG_NUMERALS = "\\p{Nd}";
	private static final String HASHTAG_SPECIAL_CHARS = "_";//underscore

	private static final String HASHTAG_LETTERS_NUMERALS = HASHTAG_LETTERS + HASHTAG_NUMERALS + HASHTAG_SPECIAL_CHARS;
	private static final String HASHTAG_LETTERS_SET = "[" + HASHTAG_LETTERS + "]";
	private static final String HASHTAG_LETTERS_NUMERALS_SET = "[" + HASHTAG_LETTERS_NUMERALS + "]";

	/* URL related hash regex collection */
	private static final String URL_VALID_PRECEEDING_CHARS = "(?:[^A-Z0-9@＠$#＃\u202A-\u202E]|^)";

	private static final String URL_VALID_CHARS = "[\\p{Alnum}" + "]";
	private static final String URL_VALID_SUBDOMAIN = "(?>(?:" + URL_VALID_CHARS + "[" + URL_VALID_CHARS + "\\-_]*)?" + URL_VALID_CHARS + "\\.)";
	private static final String URL_VALID_DOMAIN_NAME = "(?:(?:" + URL_VALID_CHARS + "[" + URL_VALID_CHARS + "\\-]*)?" + URL_VALID_CHARS + "\\.)";
	/* Any non-space, non-punctuation characters. \p{Z} = any kind of whitespace or invisible separator. */
	private static final String URL_VALID_UNICODE_CHARS = "[.[^\\p{Punct}\\s\\p{Z}\\p{InGeneralPunctuation}]]";
	private static final String URL_PUNYCODE = "(?:xn--[0-9a-z]+)";
	private static final String SPECIAL_URL_VALID_CCTLD = "(?:(?:" + "co|tv" + ")(?=[^\\p{Alnum}@]|$))";

	private static final String URL_VALID_DOMAIN =
	  "(?:" +                                                   // subdomains + domain
	      URL_VALID_SUBDOMAIN + "+" + URL_VALID_DOMAIN_NAME +   // e.g. www.twitter.com, foo.co.jp, bar.co.uk
	      "(?:" + URL_PUNYCODE + ")" +
	      ")" +
		  "|(?:" +                                                  // domain + gTLD + some ccTLD
		    URL_VALID_DOMAIN_NAME +                                 // e.g. twitter.com
		   "(?:" + URL_PUNYCODE + "|" + SPECIAL_URL_VALID_CCTLD + ")" +
		    ")" +
		    "|(?:" + "(?<=https?://)" +
		      "(?:" +
		        "(?:" + URL_VALID_DOMAIN_NAME + ")" +  // protocol + domain + ccTLD
		        "|(?:" +
		          URL_VALID_UNICODE_CHARS + "+\\." +                     // protocol + unicode domain + TLD
		        ")" +
		      ")" +
		    ")" +
		    "|(?:" +                                                  // domain + ccTLD + '/'
		      URL_VALID_DOMAIN_NAME + "(?=/)" +     // e.g. t.co/
		    ")";

	private static final String URL_VALID_PORT_NUMBER = "[0-9]++";
    private static final String URL_VALID_GENERAL_PATH_CHARS = "[a-z\\p{IsCyrillic}0-9!\\*';:=\\+,.\\$/%#\\[\\]\\-_~\\|&@" + "]";
		  /** Allow URL paths to contain up to two nested levels of balanced parens
		   *  1. Used in Wikipedia URLs like /Primer_(film)
		   *  2. Used in IIS sessions like /S(dfd346)/
		   *  3. Used in Rdio URLs like /track/We_Up_(Album_Version_(Edited))/
		  **/
	private static final String URL_BALANCED_PARENS = "\\(" +
		    "(?:" +
		      URL_VALID_GENERAL_PATH_CHARS + "+" +
		      "|" +
		      // allow one nested level of balanced parentheses
		      "(?:" +
		        URL_VALID_GENERAL_PATH_CHARS + "*" +
		        "\\(" +
		          URL_VALID_GENERAL_PATH_CHARS + "+" +
		        "\\)" +
		        URL_VALID_GENERAL_PATH_CHARS + "*" +
		      ")" +
		    ")" +
		  "\\)";

		  /** Valid end-of-path characters (so /foo. does not gobble the period).
		   *   2. Allow =&# for empty URL parameters and other URL-join artifacts
		  **/
	private static final String URL_VALID_PATH_ENDING_CHARS = "[a-z\\p{IsCyrillic}0-9=_#/\\-\\+" + "]|(?:" + URL_BALANCED_PARENS + ")";

	private static final String URL_VALID_PATH = "(?:" +
		    "(?:" +
		      URL_VALID_GENERAL_PATH_CHARS + "*" +
		      "(?:" + URL_BALANCED_PARENS + URL_VALID_GENERAL_PATH_CHARS + "*)*" +
		      URL_VALID_PATH_ENDING_CHARS +
		    ")|(?:@" + URL_VALID_GENERAL_PATH_CHARS + "+/)" +
		  ")";

	private static final String URL_VALID_URL_QUERY_CHARS = "[a-z0-9!?\\*'\\(\\);:&=\\+\\$/%#\\[\\]\\-_\\.,~\\|@]";
	private static final String URL_VALID_URL_QUERY_ENDING_CHARS = "[a-z0-9_&=#/]";
	private static final String VALID_URL_PATTERN_STRING =
		  "(" +                                                            //  $1 total match
		    "(" + URL_VALID_PRECEEDING_CHARS + ")" +                       //  $2 Preceeding chracter
		    "(" +                                                          //  $3 URL
		      "(https?://)?" +                                             //  $4 Protocol (optional)
		      "(" + URL_VALID_DOMAIN + ")" +                               //  $5 Domain(s)
		      "(?::(" + URL_VALID_PORT_NUMBER + "))?" +                     //  $6 Port number (optional)
		      "(/" +
		        URL_VALID_PATH + "*+" +
		      ")?" +                                                       //  $7 URL Path and anchor
		      "(\\?" + URL_VALID_URL_QUERY_CHARS + "*" +                   //  $8 Query String
		              URL_VALID_URL_QUERY_ENDING_CHARS + ")?" +
		    ")" +
		  ")";

	private static final String AT_SIGNS_CHARS = "@\uFF20";

	/* Begin public constants */
	public static final Pattern VALID_HASHTAG;
		  public static final int VALID_HASHTAG_GROUP_BEFORE = 1;
		  public static final int VALID_HASHTAG_GROUP_HASH = 2;
		  public static final int VALID_HASHTAG_GROUP_TAG = 3;
		  public static final Pattern INVALID_HASHTAG_MATCH_END;
		  public static final Pattern RTL_CHARACTERS;

		  public static final Pattern AT_SIGNS;
		  public static final Pattern VALID_MENTION_OR_LIST;
		  public static final int VALID_MENTION_OR_LIST_GROUP_BEFORE = 1;
		  public static final int VALID_MENTION_OR_LIST_GROUP_AT = 2;
		  public static final int VALID_MENTION_OR_LIST_GROUP_USERNAME = 3;
		  public static final int VALID_MENTION_OR_LIST_GROUP_LIST = 4;

		  public static final Pattern VALID_REPLY;
		  public static final int VALID_REPLY_GROUP_USERNAME = 1;

		  public static final Pattern INVALID_MENTION_MATCH_END;

		  /**
		   * Regex to extract URL (it also includes the text preceding the url).
		   *
		   * This regex does not reflect its name and {@link Regex#VALID_URL_GROUP_URL} match
		   * should be checked in order to match a valid url. This is not ideal, but the behavior is
		   * being kept to ensure backwards compatibility. Ideally this regex should be
		   * implemented with a negative lookbehind as opposed to a negated character class
		   * but lack of JS support increases maint overhead if the logic is different by
		   * platform.
		   */

		  public static final Pattern VALID_URL;
		  public static final int VALID_URL_GROUP_ALL          = 1;
		  public static final int VALID_URL_GROUP_BEFORE       = 2;
		  public static final int VALID_URL_GROUP_URL          = 3;
		  public static final int VALID_URL_GROUP_PROTOCOL     = 4;
		  public static final int VALID_URL_GROUP_DOMAIN       = 5;
		  public static final int VALID_URL_GROUP_PORT         = 6;
		  public static final int VALID_URL_GROUP_PATH         = 7;
		  public static final int VALID_URL_GROUP_QUERY_STRING = 8;

		  public static final Pattern VALID_TCO_URL;
		  public static final Pattern INVALID_URL_WITHOUT_PROTOCOL_MATCH_BEGIN;

		  public static final Pattern VALID_DOMAIN;

		  // initializing in a static synchronized block, there appears to be thread safety issues with Pattern.compile in android
		  static {
		    synchronized(Regex.class) {
		      VALID_HASHTAG = Pattern.compile("(^|[^&" + HASHTAG_LETTERS_NUMERALS + "])(#|\uFF03)(?!\uFE0F|\u20E3)(" + HASHTAG_LETTERS_NUMERALS_SET + "*" + HASHTAG_LETTERS_SET + HASHTAG_LETTERS_NUMERALS_SET + "*)", Pattern.CASE_INSENSITIVE);
		      INVALID_HASHTAG_MATCH_END = Pattern.compile("^(?:[#＃]|://)");
		      RTL_CHARACTERS = Pattern.compile("[\u0600-\u06FF\u0750-\u077F\u0590-\u05FF\uFE70-\uFEFF]");
		      AT_SIGNS = Pattern.compile("[" + AT_SIGNS_CHARS + "]");
		      VALID_MENTION_OR_LIST = Pattern.compile("([^a-z0-9_!#$%&*" + AT_SIGNS_CHARS + "]|^|(?:^|[^a-z0-9_+~.-])RT:?)(" + AT_SIGNS + "+)([a-z0-9_]{1,20})(/[a-z][a-z0-9_\\-]{0,24})?", Pattern.CASE_INSENSITIVE);
		      VALID_REPLY = Pattern.compile("^(?:" + UNICODE_SPACES + ")*" + AT_SIGNS + "([a-z0-9_]{1,20})", Pattern.CASE_INSENSITIVE);
		      INVALID_MENTION_MATCH_END = Pattern.compile("^(?:[" + AT_SIGNS_CHARS + "]|://)");
		      INVALID_URL_WITHOUT_PROTOCOL_MATCH_BEGIN = Pattern.compile("[-_./]$");
		      
		      VALID_URL = Pattern.compile(VALID_URL_PATTERN_STRING, Pattern.CASE_INSENSITIVE);
		      VALID_TCO_URL = Pattern.compile("^https?:\\/\\/t\\.co\\/[a-z0-9]+", Pattern.CASE_INSENSITIVE);
		      VALID_DOMAIN = Pattern.compile(URL_VALID_DOMAIN, Pattern.CASE_INSENSITIVE);
		    }
		  }

		  private static String join(Collection<?> col, String delim) {
		    final StringBuilder sb = new StringBuilder();
		    final Iterator<?> iter = col.iterator();
		    if (iter.hasNext())
		      sb.append(iter.next().toString());
		    while (iter.hasNext()) {
		      sb.append(delim);
		      sb.append(iter.next().toString());
		    }
		    return sb.toString();
		  }
}
