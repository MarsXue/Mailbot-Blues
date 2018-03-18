package strategies;

import java.util.*;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

public class PQMailPool implements IMailPool {
	
	private PriorityQueue<MailItem> mailPool;
	
	private static final int MAX_TAKE = 4;

	public PQMailPool() {
		
		mailPool = new PriorityQueue<MailItem>(1000, new Comparator<MailItem>() {
			
			@Override
			public int compare(MailItem m1, MailItem m2) {
				int m1Value = m1.getArrivalTime();
				int m2Value = m2.getArrivalTime();
				
				if (m1 instanceof PriorityMailItem) {
					m1Value /= ((PriorityMailItem) m1).getPriorityLevel();
				}
				if (m2 instanceof PriorityMailItem) {
					m2Value /= ((PriorityMailItem)m2).getPriorityLevel();
				}
				return m1Value - m2Value;
			}
		});
		
	}

	public void addToPool(MailItem mailItem) {
		mailPool.add(mailItem);
	}
	
	private int getPoolSize() {
		return mailPool.size();
	}
	
	private MailItem getMail(){
		if (getPoolSize() > 0){
			return mailPool.poll();
		}
		else{
			return null;
		}
		
	}

	@Override
	public void fillStorageTube(StorageTube tube, boolean strong) {
		int max = strong ? Integer.MAX_VALUE : 2000; // max weight
		
		try {
			while (!tube.isEmpty()) {
				addToPool(tube.pop());
			}
			
			int weight = 0;
			PriorityQueue<MailItem> queue = new PriorityQueue<MailItem>(4, new Comparator<MailItem>() {

				@Override
				public int compare(MailItem m1, MailItem m2) {
					int m1Value = m1.getDestFloor();
					int m2Value = m2.getDestFloor();
					
					return m1Value - m2Value;
				}
			});
			
			// Check for a top priority item
			while (queue.size() < MAX_TAKE && getPoolSize() > 0 && weight < max) {

				MailItem mail = getMail();
//				System.out.println(mail.getArrivalTime());
				weight += mail.getWeight();
			
				if (weight < max) {
					queue.add(mail);
//					tube.addItem(mail);
				} else {
					addToPool(mail);
				}
			}

//			while(queue.size() > 0 && tube.getSize() < MAX_TAKE) {
//				tube.addItem(queue.poll());
//			}
	
			Stack<MailItem> tmp = new Stack<MailItem>();
			while (queue.size() > 0) {
				tmp.push(queue.poll());
			}
			
			while (tmp.size() > 0 && tube.getSize() < MAX_TAKE) {
				MailItem mail = tmp.pop();
				tube.addItem(mail);
			}

		} catch (TubeFullException e) {
			e.printStackTrace();
		}
		
		
	}

}
