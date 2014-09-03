package pt.up.fe.labtablet.api;

public interface AsyncTaskHandler<T> {

	public void onSuccess(T result);

	public void onFailure(Exception error);
	
	public void onProgressUpdate(int value);

}
