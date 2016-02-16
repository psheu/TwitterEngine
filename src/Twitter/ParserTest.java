package Twitter;

import static org.junit.Assert.*;
import org.junit.Test;

//runs tests to chekc the validity of tweets, mentions, hashtags, and URLs
public class ParserTest {	
	//tests mentions in tweet
	@Test
	public void isMentionValid() {
		Parser message = new Parser("@franky @franky1 @franky2 @fr@nky$*");
		assertTrue(message.hasMention("franky"));
		assertFalse(message.hasMention("fr@nky$*"));
		//assertEquals(message.numMentions(), 3);
	}
	
	//tests hashtags in tweet
	@Test
	public void isHashtagValid(){
		Parser message = new Parser("#hollywood #hollywood1 #hollywood@$*");
		assertTrue(message.hasHashtag("hollywood1"));
		assertFalse(message.hasHashtag("hollywood@$*"));
		//assertEquals(message.numHashtags(), 2);
	}
	
	//tests URLs in tweet
	@Test
	public void isURLValid(){
		Parser message = new Parser("http://www.cnn.com http://luc.com/S@#*");
		assertTrue(message.hasUrl("http://www.cnn.com"));
		assertFalse(message.hasUrl("http://luc.com/S@#*"));
		//assertEquals(message.numUrls(), 1);
	}
	
	//tests the length of tweet
	@Test
	public void isTweet0(){
		Parser message = new Parser("@franky goes to #hollywood "
				+ "@franky goes to #hollywood "
				+ "@franky goes to #hollywood "
				+ "@franky goes to #hollywood "
				+ "@franky goes to #hollywood "
				+ "@franky goes to #hollywood "
				+ "@franky goes to #hollywood ");
		assertFalse(message.isValidTweet());
	}
	
	//tests tweet with valid and invalid mentions, hashtags, URLs
	@Test
	public void isTweet1(){
		Parser message = new Parser("@fr@nky$ goes to #hollywood http://www.cnn.com http//luc");
		assertTrue(message.isValidTweet());
		assertFalse(message.hasMention("fr@nky$"));
		assertTrue(message.hasHashtag("hollywood"));
		assertTrue(message.hasUrl("http://www.cnn.com"));
		assertFalse(message.hasUrl("http//luc"));
		//assertEquals(message.numMentions(),0);
		//assertEquals(message.numHashtags(), 1);
		//assertEquals(message.numUrls(), 1);
	}
}
