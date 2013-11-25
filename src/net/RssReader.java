package net;

import java.util.Calendar;
import java.util.List;

import net.rss.Feed;
import net.rss.FeedMessage;
import net.rss.RSSFeedParser;

public class RssReader {
	
	private Calendar _lastUpdate;
	private Feed _lastData;
	private String _url;
	private RSSFeedParser _rssReader;
	
	public RssReader(String url) {
		_url = url;
		_rssReader = new RSSFeedParser(url);
	}
	 
	public Feed getFeed() {
		updateData();
    	return _lastData;
    }
	
	public String getLastMessages(int count, String separator) {
		updateData();

		if (_lastData == null)
			return "";
		
		String result = _lastData.getTitle().trim();
		List<FeedMessage> messages = _lastData.getMessages();
		for (int i=0; i<Math.min(count, messages.size()); i++) {
			result += separator + messages.get(i).getTitle().trim();
		}
		return result;
	}
    
    private void updateData() {
    	if (_lastData != null && lastResultValid(_lastUpdate, 1))
    		return;

		try
		{
			System.out.println("Reading RSS Feed: " + _url);
	       _lastData = _rssReader.readFeed();
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    	_lastData = null;
		    _lastUpdate = Calendar.getInstance();
	    }
    }
    
   
	private boolean lastResultValid(Calendar cacheDate, int cacheTimeout) {
		if (cacheDate == null)
			return false;
		Calendar now = Calendar.getInstance();
		return (Math.abs(now.getTime().getTime() - cacheDate.getTime().getTime()) / (1000*60)) < 5;
	}

}
