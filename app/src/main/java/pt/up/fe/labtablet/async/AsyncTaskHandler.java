package pt.up.fe.labtablet.async;

public interface AsyncTaskHandler<T> {

	public void onSuccess(T result);

	public void onFailure(Exception error);
	
	public void onProgressUpdate(int value);

}
