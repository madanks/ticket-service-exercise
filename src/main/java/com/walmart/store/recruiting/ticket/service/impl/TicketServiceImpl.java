package com.walmart.store.recruiting.ticket.service.impl;

import com.walmart.store.recruiting.ticket.domain.SeatHold;
import com.walmart.store.recruiting.ticket.domain.Venue;
import com.walmart.store.recruiting.ticket.service.TicketService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A ticket service implementation.
 */
/**
 * @author Madan
 *
 */

/**
 * In this application I had very limited time so I could not track all the seats 
 * of the reservation. But I can do it in a very effective way if I get the chance
 * to enhance this project.
 *
 */

public class TicketServiceImpl implements TicketService {

	private int seatsAvailable;
	private int seatsReserved;
	private Map<String, SeatHold> seatHoldMap = new HashMap<>();

	private Map<String, Date> trackExpire = new HashMap<>();

	public TicketServiceImpl(Venue venue) {
		seatsAvailable = venue.getMaxSeats();
	}

	@Override
	public int numSeatsAvailable() {
		return seatsAvailable;
	}

	public int numSeatsReserved() {
		return this.seatsReserved;
	}

	@Override
/* for now I have managed the seat hold using Map but if I get more time then i will 
 * do this task using Http session and use locking concept so that other user cannot access 
 * until they get released*/
	public Optional<SeatHold> findAndHoldSeats(int numSeats) {
		Optional<SeatHold> optionalSeatHold = Optional.empty();
		//it checks map if the some other hold are expired or not.
		checkAllExpire();
		
		if (seatsAvailable >= numSeats) {
			String holdId = generateId();
			Date expireTime = expireTime();
			SeatHold seatHold = new SeatHold(holdId, numSeats);
			trackExpire.put(holdId, expireTime);
			optionalSeatHold = Optional.of(seatHold);
			seatHoldMap.put(holdId, seatHold);

			seatsAvailable -= numSeats;
		}

		return optionalSeatHold;
	}

	@Override
	/*for now the time was limited so all the holding and reserving seat are managed 
	 * using map if get more time then i would have used the spring Scheduling feature and session to solve this issue
	 * Similarly If I get time i will use Spring AOP features to remove cross cutting concerns*/
	public Optional<String> reserveSeats(String seatHoldId) {
		// check if the key is expired or not 
		if (checkOneExpiry(seatHoldId)) {
			
			Optional<String> optionalReservation = Optional.empty();
			SeatHold seatHold = seatHoldMap.get(seatHoldId);
			if (seatHold != null) {
				seatsReserved += seatHold.getNumSeats();
				optionalReservation = Optional.of(seatHold.getId());
				seatHoldMap.remove(seatHoldId);
			}

			return optionalReservation;
		} else {
			System.out.println("your " + seatHoldId + " is expired please try again");
			return null;
		}
	}

	private String generateId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * @return currrent date and time
	 */
	private Date expireTime() {
		return new Date();
	}

	/**
	 * This method is used to check if the we have some hold seats which are
	 * expired if hold seat are expired then it will rempve from map available
	 * seats number will increase
	 */
	public void checkAllExpire() {
		// to iterate map
		Iterator it = trackExpire.entrySet().iterator();
		// loop all data
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			// get date from map
			Date d = (Date) pair.getValue();
			// parse the date
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String expireDate = dateFormat.format(d.getTime() + 5000);
			String newDate = dateFormat.format(new Date());

			// this block is for checking the date is expired or not
			// if it is expired then it will be removed from map and available
			// seats are increased.

			try {
				if (dateFormat.parse(expireDate).after(dateFormat.parse(newDate))) {
					//System.out.println(pair.getKey() + "not expired");
				} else {
					//System.out.println(pair.getKey() + "expired");
					trackExpire.remove(pair.getKey());
					SeatHold expiredNoSeats = (SeatHold) seatHoldMap.get(pair.getKey());
					seatsAvailable += expiredNoSeats.getNumSeats();
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * this method is for checking Hold seat key whether it is expired or not.
	 * 
	 * @param key,
	 *            it is the key in the track expire map
	 * @return if key is expired, not found return false else return true
	 */
	public boolean checkOneExpiry(String key) {

		// return false if key does not exist.
		if (trackExpire.get(key) == null) {
			return false;
		}
		// if key exist then get date to check it is expired or not.
		Date d = (Date) trackExpire.get(key);

		// parse date to string
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String expireDate = dateFormat.format(d.getTime() + 5000);
		String newDate = dateFormat.format(new Date());

		// this block is for checking the date is expired or not
		// if it is expired then it will be removed from map and available seats
		// are increased.

		try {
			if (dateFormat.parse(expireDate).after(dateFormat.parse(newDate))) {
				//System.out.println(key + "not expired");
				return true;
			} else {
				//System.out.println(key + "expired");
				trackExpire.remove(key);
				seatsAvailable--;
				return false;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;

	}

}
