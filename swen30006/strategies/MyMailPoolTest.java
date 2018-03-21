/* SWEN30006 Software Modelling and Design
 * Author: Wenqing XUE <wenqingx>
 * Student ID: 813044
 */

package strategies;

import java.util.*;

import automail.MailItem;
import automail.PriorityMailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyMailPoolTest implements IMailPool {

	private PriorityQueue<MailItem> priorityPool;
	private PriorityQueue<MailItem> nonPriorityPool;
	private static final int MAX_TAKE = 4;

	public MyMailPoolTest() {
		
		priorityPool = new PriorityQueue<MailItem>(new Comparator<MailItem>() {
			public int compare(MailItem m1, MailItem m2) {
				if (((PriorityMailItem)m2).getPriorityLevel() != ((PriorityMailItem)m1).getPriorityLevel()) {
					return ((PriorityMailItem)m2).getPriorityLevel() - ((PriorityMailItem)m1).getPriorityLevel();
				} else if (m1.getDestFloor() != m2.getDestFloor()) {
					return m1.getDestFloor() - m2.getDestFloor();
				} else {
					return m2.getWeight() - m1.getWeight();
				}
			}
		});
		
		nonPriorityPool = new PriorityQueue<MailItem>(new Comparator<MailItem>() {
			public int compare(MailItem m1, MailItem m2) {
				if (m1.getDestFloor() != m2.getDestFloor()) {
					return m1.getDestFloor() - m2.getDestFloor();
				} else {
					return m2.getWeight() - m1.getWeight();
				}
			}
		});
	}

	public void addToPool(MailItem mailItem) {
		if (mailItem instanceof PriorityMailItem) {
			priorityPool.add(mailItem);
		} else {
			nonPriorityPool.add(mailItem);
		}
	}
	
	private int getPriorityPoolSize(int weightLimit) {
		int size = 0;
		for (MailItem mailitem : priorityPool) {
			if (mailitem.getWeight() <= weightLimit) {
				size++;
			}
		}
		return size;
	}
	
	private int getNonPriorityPoolSize(int weightLimit) {
		int size = 0;
		for (MailItem mailitem : priorityPool) {
			if (mailitem.getWeight() <= weightLimit) {
				size++;
			}
		}
		return size;
	}
	
	private MailItem getPriorityMail(int weightLimit) {
		Stack<MailItem> tmp = new Stack<MailItem>();
		
		while (priorityPool.peek().getWeight() > weightLimit) {
			tmp.push(priorityPool.poll());
		}
		MailItem mail = priorityPool.poll();
		priorityPool.addAll(tmp);
//		System.out.println(((MailItem)mail).getWeight());
		return mail;
	}

	private MailItem getNonPriorityMail(int weightLimit) {
		Stack<MailItem> tmp = new Stack<MailItem>();
		
		while (nonPriorityPool.peek().getWeight() > weightLimit) {
			tmp.push(nonPriorityPool.poll());
		}
		
		MailItem mail = nonPriorityPool.poll();
		nonPriorityPool.addAll(tmp);
//		System.out.println(((MailItem)mail).getWeight());
		return mail;
	}
	
	class floorCompareAs implements Comparator<MailItem> {
		public int compare(MailItem m1, MailItem m2) {
			return m1.getDestFloor() - m2.getDestFloor();
		}
	}
	
	class floorCompareDe implements Comparator<MailItem> {
		public int compare(MailItem m1, MailItem m2) {
			return m2.getDestFloor() - m1.getDestFloor();
		}
	}
	
	public void fillPriorityTube(LinkedList<MailItem> priorityTube, LinkedList<MailItem> nonPriorityTube, int max, int weight) {
		
		while ((nonPriorityTube.size() + priorityTube.size()) < MAX_TAKE && getPriorityPoolSize(max) > 0 && weight<max) {
			
			priorityTube.add(getPriorityMail(max));
			
//			MailItem mail = getPriorityMailOrigin(max);
//			weight = mail.getWeight();
//		
//			if (weight <= max) {
//				priorityTube.add(mail);
//			} else {
//				addToPool(mail);
//			}
		}
	}
	
	public void fillNonPriorityTube(LinkedList<MailItem> nonPriorityTube, LinkedList<MailItem> priorityTube, int max,int weight) {
		
		while ((nonPriorityTube.size() + priorityTube.size()) < MAX_TAKE && getNonPriorityPoolSize(max) > 0 && weight<max) {
			
			nonPriorityTube.add(getNonPriorityMail(max));

//			MailItem mail = getNonPriorityMailOrigin(max);
//			weight = mail.getWeight();
//
//			if (weight <= max) {
//				nonPriorityTube.add(mail);
//			} else {
//				addToPool(mail);
//			}
		}
	}

	@Override
	public void fillStorageTube(StorageTube tube, boolean strong) {
		int max = strong ? Integer.MAX_VALUE : 2000; // max weight
		try {
			while(!tube.isEmpty()) {
				addToPool(tube.pop());
			}
			int weight = 0;
			
			LinkedList<MailItem> priorityTube = new LinkedList<MailItem>();
			LinkedList<MailItem> nonPriorityTube = new LinkedList<MailItem>();
			
			fillPriorityTube(priorityTube, nonPriorityTube, max, weight);
			fillNonPriorityTube(nonPriorityTube, priorityTube, max, weight);
			
			Boolean priorityExist = (priorityTube.isEmpty()) ? false : true;
			Boolean nonPriorityExist = (nonPriorityTube.isEmpty()) ? false : true;
			
			if (priorityExist) {
				Collections.sort(priorityTube, new floorCompareAs());
				if (nonPriorityExist) {
					Collections.sort(nonPriorityTube, new floorCompareDe());
				}
			} else {
				if (nonPriorityExist) {
					Collections.sort(nonPriorityTube, new floorCompareAs());
				}
			}
			
			Stack<MailItem> rev = new Stack<MailItem>();
			
			while (rev.size() < MAX_TAKE) {
				if (!priorityTube.isEmpty()) {
					rev.push(priorityTube.poll());
				} else if (!nonPriorityTube.isEmpty()) {
					rev.push(nonPriorityTube.poll());
				} else {
					break;
				}
			}
			
			while (tube.getSize() < MAX_TAKE && rev.size() > 0) {
				tube.addItem(rev.pop());	
				
				MailItem look = tube.peek();
				System.out.print("Floor: " + look.getDestFloor() + "  Arrival: " + look.getArrivalTime());
				if (look instanceof PriorityMailItem) {
					System.out.print(" --- " + ((PriorityMailItem)look).getPriorityLevel());
				}
				System.out.println();
			}
		} catch (TubeFullException e) {
			e.printStackTrace();
		}
	}
}