package xpadro.spring.jms.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import xpadro.spring.jms.model.Notification;

/**
 * Tests JMS message sending within local transactions
 * 
 * @author xpadro
 *
 */
@ContextConfiguration(locations = {"/xpadro/spring/jms/config/tx-jms-config.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestTransactedMessaging extends TestBaseMessaging {
	private static Logger logger = LoggerFactory.getLogger(TestTransactedMessaging.class);
	
	/**
	 * Tests the correct sending and receiving of a notification message, saving it to the DB
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testCorrectMessage() throws InterruptedException {
		Notification notification = new Notification(0, "notification to deliver correctly");
		producer.convertAndSendMessage(QUEUE_INCOMING, notification);
		
		Thread.sleep(6000);
		printResults();
		
		assertEquals(1, getSavedNotifications());
		assertEquals(0, getMessagesInQueue(QUEUE_INCOMING));
		assertEquals(0, getMessagesInQueue(QUEUE_DLQ));
	}
	
	/**
	 * Tests a failure after receiving the message and before saving it to the DB
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testFailedAfterReceiveMessage() throws InterruptedException {
		Notification notification = new Notification(1, "notification to fail after receiving");
		producer.convertAndSendMessage(QUEUE_INCOMING, notification);
		
		Thread.sleep(6000);
		printResults();
		
		assertEquals(0, getSavedNotifications());
		assertEquals(0, getMessagesInQueue(QUEUE_INCOMING));
		assertEquals(1, getMessagesInQueue(QUEUE_DLQ));
		//Empty the dead letter queue
		jmsTemplate.receive(QUEUE_DLQ);
	}
	
	@Test
	public void testFailedAfterProcessingMessage() throws InterruptedException {
		Notification notification = new Notification(2, "notification to fail after processing");
		producer.convertAndSendMessage(QUEUE_INCOMING, notification);
		
		Thread.sleep(6000);
		printResults();
		
		assertEquals(2, getSavedNotifications());
		assertEquals(0, getMessagesInQueue(QUEUE_INCOMING));
		assertEquals(0, getMessagesInQueue(QUEUE_DLQ));
	}
	
	private void printResults() {
		logger.info("Total items in \"incoming\" queue: "+getMessagesInQueue(QUEUE_INCOMING));
		logger.info("Total items in \"dead letter\" queue: "+getMessagesInQueue(QUEUE_DLQ));
		logger.info("Total items in DB: "+getSavedNotifications());
	}
}
