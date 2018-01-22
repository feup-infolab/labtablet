package pt.up.fe.beta.labtablet.async;

/**
 * interface for the asynctasks in use in this application
 * @param <T> Object type
 */
public interface AsyncTaskHandler<T> {

	void onSuccess(T result);

	void onFailure(Exception error);
	
	void onProgressUpdate(int value);

}
