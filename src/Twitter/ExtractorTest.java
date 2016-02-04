package Twitter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Twitter.Extractor.Entity.Type;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

public class ExtractorTest extends TestCase {
	protected Extractor extractor;

	  public static Test suite() {
	    Class<?>[] testClasses = { OffsetConversionTest.class,
	            MentionTest.class, HashtagTest.class};
	    return new TestSuite(testClasses);
	  }

	  public void setUp() throws Exception {
	    extractor = new Extractor();
	  }

	  public static class OffsetConversionTest extends ExtractorTest {

	    public void testConvertIndices() {
	      assertOffsetConversionOk("abc", "abc");
	      assertOffsetConversionOk("\ud83d\ude02abc", "abc");
	      assertOffsetConversionOk("\ud83d\ude02abc\ud83d\ude02", "abc");
	      assertOffsetConversionOk("\ud83d\ude02abc\ud838\ude02abc", "abc");
	      assertOffsetConversionOk("\ud83d\ude02abc\ud838\ude02abc\ud83d\ude02",
	              "abc");
	      assertOffsetConversionOk("\ud83d\ude02\ud83d\ude02abc", "abc");
	      assertOffsetConversionOk("\ud83d\ude02\ud83d\ude02\ud83d\ude02abc",
	              "abc");

	      assertOffsetConversionOk
	              ("\ud83d\ude02\ud83d\ude02\ud83d\ude02abc\ud83d\ude02", "abc");

	      // Several surrogate pairs following the entity
	      assertOffsetConversionOk
	              ("\ud83d\ude02\ud83d\ude02\ud83d\ude02abc\ud83d\ude02\ud83d" +
	                      "\ude02\ud83d\ude02", "abc");

	      // Several surrogate pairs surrounding multiple entities
	      assertOffsetConversionOk
	              ("\ud83d\ude02\ud83d\ude02\ud83d\ude02\ud83d\ude02abc\ud83d" +
	                      "\ude02\ud83d\ude02\ud83d\ude02\ud83d\ude02abc\ud83d" +
	                      "\ude02\ud83d\ude02\ud83d\ude02\ud83d\ude02", "abc");

	      // unpaired low surrogate (at start)
	      assertOffsetConversionOk
	              ("\ude02\ud83d\ude02\ud83d\ude02\ud83d\ude02abc\ud83d" +
	                      "\ude02\ud83d\ude02\ud83d\ude02\ud83d\ude02abc\ud83d" +
	                      "\ude02\ud83d\ude02\ud83d\ude02\ud83d\ude02", "abc");

	      // unpaired low surrogate (at end)
	      assertOffsetConversionOk
	              ("\ud83d\ude02\ud83d\ude02\ud83d\ude02\ud83d\ude02abc\ud83d" +
	                      "\ude02\ud83d\ude02\ud83d\ude02\ud83d\ude02abc\ud83d" +
	                      "\ude02\ud83d\ude02\ud83d\ude02\ude02", "abc");

	      // unpaired low and high surrogates (at end)
	      assertOffsetConversionOk
	              ("\ud83d\ude02\ud83d\ude02\ud83d\ude02\ud83d\ude02abc\ud83d" +
	                      "\ude02\ud83d\ude02\ud83d\ude02\ud83d\ude02abc\ud83d" +
	                      "\ude02\ud83d\ude02\ud83d\ud83d\ude02\ude02", "abc");

	      assertOffsetConversionOk("\ud83dabc\ud83d", "abc");

	      assertOffsetConversionOk("\ude02abc\ude02", "abc");

	      assertOffsetConversionOk("\ude02\ude02abc\ude02\ude02", "abc");

	      assertOffsetConversionOk("abcabc", "abc");

	      assertOffsetConversionOk("abc\ud83d\ude02abc", "abc");

	      assertOffsetConversionOk("aa", "a");

	      assertOffsetConversionOk("\ud83d\ude02a\ud83d\ude02a\ud83d\ude02", "a");
	    }

	    private void assertOffsetConversionOk(String testData, String patStr) {
	      // Build an entity at the location of patStr
	      final Pattern pat = Pattern.compile(patStr);
	      final Matcher matcher = pat.matcher(testData);

	      List<Extractor.Entity> entities = new ArrayList<Extractor.Entity>();
	      List<Integer> codePointOffsets = new ArrayList<Integer>();
	      List<Integer> charOffsets = new ArrayList<Integer>();
	      while (matcher.find()) {
	        final int charOffset = matcher.start();
	        charOffsets.add(charOffset);
	        codePointOffsets.add(testData.codePointCount(0, charOffset));
	        entities.add(new Extractor.Entity(matcher, Type.HASHTAG, 0, 0));
	      }
	    }
	  }


	  /**
	   * Tests for the extractMentionedScreennames{WithIndices} methods
	   */
	  public static class MentionTest extends ExtractorTest {
	    public void testMentionAtTheBeginning() {
	      List<String> extracted = extractor.extractMentionedScreennames("@user mention");
	      assertList("Failed to extract mention at the beginning", new String[]{"user"}, extracted);
	    }

	    public void testMentionWithLeadingSpace() {
	      List<String> extracted = extractor.extractMentionedScreennames(" @user mention");
	      assertList("Failed to extract mention with leading space", new String[]{"user"}, extracted);
	    }

	    public void testMentionInMidText() {
	      List<String> extracted = extractor.extractMentionedScreennames("mention @user here");
	      assertList("Failed to extract mention in mid text", new String[]{"user"}, extracted);
	    }

	    public void testMultipleMentions() {
	      List<String> extracted = extractor.extractMentionedScreennames("mention @user1 here and @user2 here");
	      assertList("Failed to extract multiple mentioned users", new String[]{"user1", "user2"}, extracted);
	    }

	    public void testMentionWithIndices() {
	      List<Extractor.Entity> extracted = extractor.extractMentionedScreennamesWithIndices(" @user1 mention @user2 here @user3 ");
	      assertEquals(extracted.size(), 3);
	      assertEquals(extracted.get(0).getStart().intValue(), 1);
	      assertEquals(extracted.get(0).getEnd().intValue(), 7);
	      assertEquals(extracted.get(1).getStart().intValue(), 16);
	      assertEquals(extracted.get(1).getEnd().intValue(), 22);
	      assertEquals(extracted.get(2).getStart().intValue(), 28);
	      assertEquals(extracted.get(2).getEnd().intValue(), 34);
	    }

	    public void testMentionWithSupplementaryCharacters() {
	      // insert U+10400 before " @mention"
	      String text = String.format("%c @mention %c @mention", 0x00010400, 0x00010400);

	      // count U+10400 as 2 characters (as in UTF-16)
	      List<Extractor.Entity> extracted = extractor.extractMentionedScreennamesWithIndices(text);
	      assertEquals(extracted.size(), 2);
	      assertEquals(extracted.get(0).value, "mention");
	      assertEquals(extracted.get(0).start, 3);
	      assertEquals(extracted.get(0).end, 11);
	      assertEquals(extracted.get(1).value, "mention");
	      assertEquals(extracted.get(1).start, 15);
	      assertEquals(extracted.get(1).end, 23);
	    }
	  }

	   /**
	   * Tests for the extractHashtags method
	   */
	  public static class HashtagTest extends ExtractorTest {
	    public void testHashtagAtTheBeginning() {
	      List<String> extracted = extractor.extractHashtags("#hashtag mention");
	      assertList("Failed to extract hashtag at the beginning", new String[]{"hashtag"}, extracted);
	    }

	    public void testHashtagWithLeadingSpace() {
	      List<String> extracted = extractor.extractHashtags(" #hashtag mention");
	      assertList("Failed to extract hashtag with leading space", new String[]{"hashtag"}, extracted);
	    }

	    public void testHashtagInMidText() {
	      List<String> extracted = extractor.extractHashtags("mention #hashtag here");
	      assertList("Failed to extract hashtag in mid text", new String[]{"hashtag"}, extracted);
	    }

	    public void testMultipleHashtags() {
	      List<String> extracted = extractor.extractHashtags("text #hashtag1 #hashtag2");
	      assertList("Failed to extract multiple hashtags", new String[]{"hashtag1", "hashtag2"}, extracted);
	    }

	    public void testHashtagWithIndices() {
	      List<Extractor.Entity> extracted = extractor.extractHashtagsWithIndices(" #user1 mention #user2 here #user3 ");
	      assertEquals(extracted.size(), 3);
	      assertEquals(extracted.get(0).getStart().intValue(), 1);
	      assertEquals(extracted.get(0).getEnd().intValue(), 7);
	      assertEquals(extracted.get(1).getStart().intValue(), 16);
	      assertEquals(extracted.get(1).getEnd().intValue(), 22);
	      assertEquals(extracted.get(2).getStart().intValue(), 28);
	      assertEquals(extracted.get(2).getEnd().intValue(), 34);
	    }

	    public void testHashtagWithSupplementaryCharacters() {
	      // insert U+10400 before " #hashtag"
	      String text = String.format("%c #hashtag %c #hashtag", 0x00010400, 0x00010400);

	      // count U+10400 as 2 characters (as in UTF-16)
	      List<Extractor.Entity> extracted = extractor.extractHashtagsWithIndices(text);
	      assertEquals(extracted.size(), 2);
	      assertEquals(extracted.get(0).value, "hashtag");
	      assertEquals(extracted.get(0).start, 3);
	      assertEquals(extracted.get(0).end, 11);
	      assertEquals(extracted.get(1).value, "hashtag");
	      assertEquals(extracted.get(1).start, 15);
	      assertEquals(extracted.get(1).end, 23);
	    }
	  }

	  public void testUrlWithSpecialCCTLDWithoutProtocol() {
	    String text = "MLB.tv vine.co";
	    assertList("Failed to extract URLs without protocol",
	        new String[]{"MLB.tv", "vine.co"}, extractor.extractURLs(text));

	    List<Extractor.Entity> extracted = extractor.extractURLsWithIndices(text);
	    assertEquals(extracted.get(0).getStart().intValue(), 0);
	    assertEquals(extracted.get(0).getEnd().intValue(), 6);
	    assertEquals(extracted.get(1).getStart().intValue(), 7);
	    assertEquals(extracted.get(1).getEnd().intValue(), 14);

	    extractor.setExtractURLWithoutProtocol(false);
	    assertTrue("Should not extract URLs w/o protocol", extractor.extractURLs(text).isEmpty());
	  }

	  /**
	   * Helper method for asserting that the List of extracted Strings match the expected values.
	   *
	   * @param message to display on failure
	   * @param expected Array of Strings that were expected to be extracted
	   * @param actual List of Strings that were extracted
	   */
	  protected void assertList(String message, String[] expected, List<String> actual) {
	    List<String> expectedList = Arrays.asList(expected);
	    if (expectedList.size() != actual.size()) {
	      fail(message + "\n\nExpected list and extracted list are differnt sizes:\n" +
	      "  Expected (" + expectedList.size() + "): " + expectedList + "\n" +
	      "  Actual   (" + actual.size() + "): " + actual);
	    } else {
	      for (int i=0; i < expectedList.size(); i++) {
	        assertEquals(expectedList.get(i), actual.get(i));
	      }
	    }
	  }
}