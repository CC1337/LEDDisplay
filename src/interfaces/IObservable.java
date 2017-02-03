package interfaces;
import java.util.Observer;

public interface IObservable {

	/**
	 * For using default Java Observable implementation methods in interfaces
	 */
	
	void addObserver(Observer o);    
    void deleteObserver(Observer o); 
}
