package pt.up.fe.alpha.labtablet.async;

/**
 * interface for the asynctasks in use in this application
 * @param <T> Object type
 */
public interface AsyncTaskHandler<T> {

	public void onSuccess(T result);

	public void onFailure(Exception error);
	
	public void onProgressUpdate(int value);

}
