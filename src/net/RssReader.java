package net;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.logging.Logger;

import net.rss.Feed;
import net.rss.FeedMessage;
import net.rss.RSSFeedParser;

public class RssReader {
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private volatile Feed _lastData;
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
    
    private final java.util.concurrent.atomic.AtomicBoolean loading = new java.util.concurrent.atomic.AtomicBoolean();
    private volatile long nextAttempt;

    private void updateData() {
        if (System.nanoTime() < nextAttempt || !loading.compareAndSet(false, true)) return;
        if (System.nanoTime() < nextAttempt) {
            loading.set(false);
            return;
        }
        Thread worker = new Thread(() -> {
            long delay = 60000;
            try {
                Feed feed = _rssReader.readFeed();
                if (feed == null) throw new IllegalStateException("Empty RSS feed");
                _lastData = feed;
                delay = 300000;
            } catch (Exception exception) {
                LOGGER.warning("RSS skipped: " + exception.getMessage());
            } finally {
                nextAttempt = System.nanoTime() + delay * 1000000L;
                loading.set(false);
            }
        }, "RSSUpd");
        worker.setDaemon(true);
        worker.start();
    }
}
